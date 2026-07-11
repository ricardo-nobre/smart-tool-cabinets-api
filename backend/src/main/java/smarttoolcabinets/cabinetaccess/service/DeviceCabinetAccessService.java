package smarttoolcabinets.cabinetaccess.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.cabinetaccess.domain.CabinetAccessStatus;
import smarttoolcabinets.inventory.domain.InventorySnapshot;
import smarttoolcabinets.inventory.domain.InventorySnapshotItem;
import smarttoolcabinets.inventory.domain.SnapshotType;
import smarttoolcabinets.inventory.repository.InventorySnapshotItemRepository;
import smarttoolcabinets.inventory.repository.InventorySnapshotRepository;
import smarttoolcabinets.inventory.service.InventoryDeltaService;
import smarttoolcabinets.cabinetaccess.domain.CabinetAccess;
import smarttoolcabinets.cabinetaccess.domain.OperationalResult;
import smarttoolcabinets.cabinetaccess.dto.CloseCabinetAccessResponse;
import smarttoolcabinets.cabinetaccess.dto.OpenCabinetAccessRequest;
import smarttoolcabinets.cabinetaccess.dto.OpenCabinetAccessResponse;
import smarttoolcabinets.cabinetaccess.repository.CabinetAccessRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.domain.ToolAssignmentStatus;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service do ciclo de vida de CabinetAccess no dispositivo.
 */
@Service
public class DeviceCabinetAccessService {

    private final CabinetAccessRepository cabinetAccessRepository;
    private final CabinetRepository cabinetRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final InventorySnapshotItemRepository inventorySnapshotItemRepository;
    private final ToolAssignmentRepository toolAssignmentRepository;
    private final InventoryDeltaService inventoryDeltaService;
    private final ToolRepository toolRepository;

    public DeviceCabinetAccessService(
            CabinetAccessRepository cabinetAccessRepository,
            CabinetRepository cabinetRepository,
            UserRepository userRepository,
            AuditService auditService,
            InventorySnapshotRepository inventorySnapshotRepository,
            InventorySnapshotItemRepository inventorySnapshotItemRepository,
            ToolAssignmentRepository toolAssignmentRepository,
            InventoryDeltaService inventoryDeltaService,
            ToolRepository toolRepository
    ) {
        this.cabinetAccessRepository = cabinetAccessRepository;
        this.cabinetRepository = cabinetRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.inventorySnapshotRepository = inventorySnapshotRepository;
        this.inventorySnapshotItemRepository = inventorySnapshotItemRepository;
        this.toolAssignmentRepository = toolAssignmentRepository;
        this.inventoryDeltaService = inventoryDeltaService;
        this.toolRepository = toolRepository;
    }

    @Transactional
    public OpenCabinetAccessResponse openCabinetAccess(OpenCabinetAccessRequest request) {
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
        if (!UserRole.OPERATOR.equalsIgnoreCase(operator.getRole())) {
            throw new IllegalArgumentException("operator must have role OPERATOR");
        }

        if (cabinetAccessRepository.findFirstByCabinetIdAndStatus(cabinet.getId(), CabinetAccessStatus.OPEN).isPresent()) {
            throw new IllegalStateException("an OPEN cabinetAccess already exists for cabinet: " + cabinet.getId());
        }

        CabinetAccess cabinetAccess = CabinetAccess.open(cabinet.getId(), operatorId);
        CabinetAccess saved = cabinetAccessRepository.save(cabinetAccess);

        auditService.logAction(
                "DEVICE",
                cabinet.getCode(),
                "OPEN_CABINET_ACCESS",
                AuditEntityType.CABINET_ACCESS,
                saved.getId()
        );

        return new OpenCabinetAccessResponse(saved.getId(), saved.getStatus(), saved.getOpenedAt());
    }

    @Transactional
    public CloseCabinetAccessResponse closeCabinetAccess(String cabinetAccessId) {
        if (cabinetAccessId == null || cabinetAccessId.isBlank()) {
            throw new IllegalArgumentException("cabinetAccessId is required");
        }

        UUID parsedCabinetAccessId;
        try {
            parsedCabinetAccessId = UUID.fromString(cabinetAccessId.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid cabinetAccessId: " + cabinetAccessId);
        }

        Optional<CabinetAccess> cabinetAccessOpt = cabinetAccessRepository.findById(parsedCabinetAccessId);
        if (cabinetAccessOpt.isEmpty()) {
            throw new IllegalArgumentException("cabinetAccess not found");
        }
        CabinetAccess cabinetAccess = cabinetAccessOpt.get();
        if (!CabinetAccessStatus.OPEN.equals(cabinetAccess.getStatus())) {
            throw new IllegalStateException("CabinetAccess is not open: " + parsedCabinetAccessId);
        }

        int assignmentsCreatedCount = 0;
        int assignmentsReturnedCount = 0;
        boolean discrepancyFlag = false;

        Optional<InventorySnapshot> beforeSnapshot = inventorySnapshotRepository
                .findByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, SnapshotType.BEFORE)
                .stream()
                .findFirst();

        Optional<InventorySnapshot> afterSnapshot = inventorySnapshotRepository
                .findByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, SnapshotType.AFTER)
                .stream()
                .findFirst();

        if (beforeSnapshot.isPresent() && afterSnapshot.isPresent()) {
            Set<UUID> beforeTools = extractRecognizedToolIds(beforeSnapshot.get().getId());
            Set<UUID> afterTools = extractRecognizedToolIds(afterSnapshot.get().getId());
            var delta = inventoryDeltaService.calculate(beforeTools, afterTools);

            for (UUID toolId : delta.removed()) {
                if (toolAssignmentRepository.existsByToolIdAndStatusIn(toolId, ToolAssignmentStatus.OPEN)) {
                    discrepancyFlag = true;
                    continue;
                }
                Tool tool = toolRepository.findById(toolId)
                        .orElseThrow(() -> new IllegalStateException("tool not found: " + toolId));
                if (!tool.isActive()) {
                    discrepancyFlag = true;
                    continue;
                }
                ToolAssignment assignment = ToolAssignment.createActive(
                        toolId,
                        cabinetAccess.getOperatorId(),
                        cabinetAccess.getCabinetId(),
                        cabinetAccess.getId(),
                        OffsetDateTime.now()
                );
                toolAssignmentRepository.save(assignment);
                assignmentsCreatedCount++;
            }

            for (UUID toolId : delta.returned()) {
                Optional<ToolAssignment> openAssignmentOpt = toolAssignmentRepository.findFirstByToolIdAndStatusIn(toolId, ToolAssignmentStatus.OPEN);
                if (openAssignmentOpt.isEmpty()) {
                    discrepancyFlag = true;
                    continue;
                }

                ToolAssignment assignment = openAssignmentOpt.get();
                if (assignment.getOriginCabinetId().equals(cabinetAccess.getCabinetId())) {
                    assignment.markReturned(cabinetAccess.getCabinetId(), cabinetAccess.getId(), OffsetDateTime.now());
                    assignmentsReturnedCount++;
                } else {
                    assignment.markPendingReview(cabinetAccess.getCabinetId(), cabinetAccess.getId(), OffsetDateTime.now());
                    discrepancyFlag = true;
                }
                toolAssignmentRepository.save(assignment);
            }
        } else {
            discrepancyFlag = true;
        }

        var cabinet = cabinetRepository.findById(cabinetAccess.getCabinetId())
                .orElseThrow(() -> new IllegalArgumentException("cabinet not found: " + cabinetAccess.getCabinetId()));

        cabinetAccess.close();
        CabinetAccess saved = cabinetAccessRepository.save(cabinetAccess);
        auditService.logAction(
                "DEVICE",
                cabinet.getCode(),
                "CLOSE_CABINET_ACCESS",
                AuditEntityType.CABINET_ACCESS,
                saved.getId()
        );

        String operationalResult;
        if (discrepancyFlag) {
            operationalResult = OperationalResult.CLOSED_WITH_DISCREPANCY;
        } else if (assignmentsCreatedCount > 0 || assignmentsReturnedCount > 0) {
            operationalResult = OperationalResult.CLOSED_WITH_ASSIGNMENTS;
        } else {
            operationalResult = OperationalResult.CLOSED_OK;
        }

        return new CloseCabinetAccessResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getClosedAt(),
                operationalResult,
                assignmentsCreatedCount,
                assignmentsReturnedCount,
                discrepancyFlag
        );
     }

    private Set<UUID> extractRecognizedToolIds(UUID snapshotId) {
        return inventorySnapshotItemRepository.findBySnapshotId(snapshotId).stream()
                .map(InventorySnapshotItem::getToolId)
                .filter(toolId -> toolId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
