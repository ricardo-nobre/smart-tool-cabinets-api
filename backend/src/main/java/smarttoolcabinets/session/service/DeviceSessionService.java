package smarttoolcabinets.session.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.inventory.domain.InventorySnapshot;
import smarttoolcabinets.inventory.domain.InventorySnapshotItem;
import smarttoolcabinets.inventory.repository.InventorySnapshotItemRepository;
import smarttoolcabinets.inventory.repository.InventorySnapshotRepository;
import smarttoolcabinets.inventory.service.InventoryDeltaService;
import smarttoolcabinets.session.domain.Session;
import smarttoolcabinets.session.dto.CloseSessionResponse;
import smarttoolcabinets.session.dto.OpenSessionRequest;
import smarttoolcabinets.session.dto.OpenSessionResponse;
import smarttoolcabinets.session.repository.SessionRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service do ciclo de vida de CabinetAccess no dispositivo.
 */
@Service
public class DeviceSessionService {

    private final SessionRepository sessionRepository;
    private final CabinetRepository cabinetRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final InventorySnapshotItemRepository inventorySnapshotItemRepository;
    private final ToolAssignmentRepository toolAssignmentRepository;
    private final InventoryDeltaService inventoryDeltaService;

    public DeviceSessionService(
            SessionRepository sessionRepository,
            CabinetRepository cabinetRepository,
            UserRepository userRepository,
            AuditService auditService,
            InventorySnapshotRepository inventorySnapshotRepository,
            InventorySnapshotItemRepository inventorySnapshotItemRepository,
            ToolAssignmentRepository toolAssignmentRepository,
            InventoryDeltaService inventoryDeltaService
    ) {
        this.sessionRepository = sessionRepository;
        this.cabinetRepository = cabinetRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.inventorySnapshotRepository = inventorySnapshotRepository;
        this.inventorySnapshotItemRepository = inventorySnapshotItemRepository;
        this.toolAssignmentRepository = toolAssignmentRepository;
        this.inventoryDeltaService = inventoryDeltaService;
    }

    /**
     * Abre CabinetAccess para o armario e operador autenticado.
     */
    @Transactional
    public OpenSessionResponse openSession(OpenSessionRequest request) {
          if (request == null) {
              throw new IllegalArgumentException("request is required");
          }

          String cabinetCode = request.cabinetCode();
          if (cabinetCode == null || cabinetCode.isBlank()) {
              throw new IllegalArgumentException("cabinetCode is required");
          }

          UUID operatorId = request.operatorId();

          var cabinet = cabinetRepository.findByCode(cabinetCode.trim())
                  .orElseThrow(() -> new IllegalArgumentException("cabinet not found for code: " + cabinetCode));

          if (!cabinet.isActive()) {
              throw new IllegalStateException("cabinet is not active: " + cabinetCode);
          }

          var operator = userRepository.findById(operatorId)
                  .orElseThrow(() -> new IllegalArgumentException("operator not found: " + operatorId));

          if (!operator.isActive()) {
              throw new IllegalStateException("operator is not active: " + operatorId);
          }
          if (!"OPERATOR".equalsIgnoreCase(operator.getRole())) {
              throw new IllegalArgumentException("operator must have role OPERATOR");
          }

          if (sessionRepository.findFirstByCabinetIdAndStatus(cabinet.getId(), "OPEN").isPresent()) {
              throw new IllegalStateException("an OPEN session already exists for cabinet: " + cabinet.getId());
          }

          Session session = Session.open(cabinet.getId(), operatorId);
          Session saved = sessionRepository.save(session);

          auditService.logAction(
                  "DEVICE:" + cabinet.getCode(),
                  "OPEN_CABINET_ACCESS",
                  AuditEntityType.CABINET_ACCESS,
                  saved.getId().toString()
          );

          return new OpenSessionResponse(saved.getId(), saved.getStatus(), saved.getOpenedAt());
      }

    /**
     * Fecha CabinetAccess e devolve resultado operacional baseline.
     */
    @Transactional
    public CloseSessionResponse closeSession(String cabinetAccessId) {
        if (cabinetAccessId == null || cabinetAccessId.isBlank()) {
            throw new IllegalArgumentException("cabinetAccessId is required");
        }

        UUID parsedCabinetAccessId;
        try {
            parsedCabinetAccessId = UUID.fromString(cabinetAccessId.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid cabinetAccessId: " + cabinetAccessId);
        }

        Optional<Session> session = sessionRepository.findById(parsedCabinetAccessId);
        if( session.isEmpty()) {
            throw new IllegalArgumentException("cabinetAccess not found");
        }
        Session s = session.get();
        if(!s.getStatus().equals("OPEN")) {
            throw new IllegalStateException("CabinetAccess is not open: " + parsedCabinetAccessId);
        }

        int assignmentsCreatedCount = 0;
        int assignmentsReturnedCount = 0;
        boolean discrepancyFlag = false;

        Optional<InventorySnapshot> beforeSnapshot = inventorySnapshotRepository
                .findByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, "BEFORE")
                .stream()
                .findFirst();

        Optional<InventorySnapshot> afterSnapshot = inventorySnapshotRepository
                .findByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, "AFTER")
                .stream()
                .findFirst();

        int unknownTagsCount = afterSnapshot
                .or(() -> inventorySnapshotRepository.findTopByCabinetAccessIdOrderByCapturedAtDesc(parsedCabinetAccessId))
                .map(snapshot -> inventorySnapshotItemRepository.findBySnapshotId(snapshot.getId()).stream()
                        .filter(item -> !item.isRecognized())
                        .map(InventorySnapshotItem::getTagCode)
                        .filter(tag -> tag != null && !tag.isBlank())
                        .map(tag -> tag.trim().toUpperCase(Locale.ROOT))
                        .distinct()
                        .toList()
                        .size())
                .orElse(0);

        if (beforeSnapshot.isPresent() && afterSnapshot.isPresent()) {
            Set<UUID> beforeTools = extractRecognizedToolIds(beforeSnapshot.get().getId());
            Set<UUID> afterTools = extractRecognizedToolIds(afterSnapshot.get().getId());
            var delta = inventoryDeltaService.calculate(beforeTools, afterTools);

            for (UUID toolId : delta.removed()) {
                if (toolAssignmentRepository.findByToolIdAndStatus(toolId, "ACTIVE").isPresent()) {
                    discrepancyFlag = true;
                    continue;
                }
                ToolAssignment assignment = ToolAssignment.createActive(
                        toolId,
                        s.getOperatorId(),
                        s.getCabinetId(),
                        s.getId(),
                        OffsetDateTime.now()
                );
                toolAssignmentRepository.save(assignment);
                assignmentsCreatedCount++;
            }

            for (UUID toolId : delta.returned()) {
                Optional<ToolAssignment> activeAssignmentOpt = toolAssignmentRepository.findByToolIdAndStatus(toolId, "ACTIVE");
                if (activeAssignmentOpt.isEmpty()) {
                    discrepancyFlag = true;
                    continue;
                }

                ToolAssignment assignment = activeAssignmentOpt.get();
                if (assignment.getOriginCabinetId().equals(s.getCabinetId())) {
                    assignment.markReturned(s.getCabinetId(), s.getId(), OffsetDateTime.now());
                    assignmentsReturnedCount++;
                } else {
                    assignment.markPendingReview();
                    discrepancyFlag = true;
                }
                toolAssignmentRepository.save(assignment);
            }
        } else {
            discrepancyFlag = true;
        }

        s.close();
        Session saved = sessionRepository.save(s);
        auditService.logAction(
                "CABINET_ACCESS:" + saved.getId(),
                "CLOSE_CABINET_ACCESS",
                AuditEntityType.CABINET_ACCESS,
                saved.getId().toString()
        );

        String operationalResult;
        if (discrepancyFlag) {
            operationalResult = "CLOSED_WITH_DISCREPANCY";
        } else if (unknownTagsCount > 0) {
            operationalResult = "CLOSED_WITH_UNKNOWN_TAGS";
        } else if (assignmentsCreatedCount > 0 || assignmentsReturnedCount > 0) {
            operationalResult = "CLOSED_WITH_ASSIGNMENTS";
        } else {
            operationalResult = "CLOSED_OK";
        }

        return new CloseSessionResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getClosedAt(),
                operationalResult,
                assignmentsCreatedCount,
                assignmentsReturnedCount,
                unknownTagsCount,
                discrepancyFlag
        );
     }

    private Set<UUID> extractRecognizedToolIds(UUID snapshotId) {
        return inventorySnapshotItemRepository.findBySnapshotId(snapshotId).stream()
                .filter(InventorySnapshotItem::isRecognized)
                .map(InventorySnapshotItem::getToolId)
                .filter(toolId -> toolId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}


