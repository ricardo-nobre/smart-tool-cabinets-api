package smarttoolcabinets.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.inventory.domain.InventorySnapshot;
import smarttoolcabinets.inventory.domain.InventorySnapshotItem;
import smarttoolcabinets.inventory.domain.SnapshotType;
import smarttoolcabinets.inventory.dto.CreateSnapshotRequest;
import smarttoolcabinets.inventory.dto.CreateSnapshotResponse;
import smarttoolcabinets.inventory.repository.InventorySnapshotItemRepository;
import smarttoolcabinets.inventory.repository.InventorySnapshotRepository;
import smarttoolcabinets.cabinetaccess.domain.CabinetAccess;
import smarttoolcabinets.cabinetaccess.domain.CabinetAccessStatus;
import smarttoolcabinets.cabinetaccess.repository.CabinetAccessRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de inventario para snapshots e avaliacao de presenca de ferramentas.
 */
@Service
public class InventoryService {

    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final InventorySnapshotItemRepository inventorySnapshotItemRepository;
    private final CabinetAccessRepository cabinetAccessRepository;
    private final AuditService auditService;
    private final ToolRepository toolRepository;


    public InventoryService(InventorySnapshotRepository inventorySnapshotRepository, CabinetAccessRepository cabinetAccessRepository, AuditService auditService, ToolRepository toolRepository, InventorySnapshotItemRepository inventorySnapshotItemRepository) {
        this.inventorySnapshotRepository = inventorySnapshotRepository;
        this.cabinetAccessRepository = cabinetAccessRepository;
        this.auditService = auditService;
        this.toolRepository = toolRepository;
        this.inventorySnapshotItemRepository = inventorySnapshotItemRepository;
    }

    @Transactional
    public CreateSnapshotResponse createSnapshot(String cabinetAccessId, CreateSnapshotRequest request) {
        if (cabinetAccessId == null || cabinetAccessId.isBlank()) {
            throw new IllegalArgumentException("cabinetAccessId is required");
        }

        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        UUID parsedCabinetAccessId;
        try {
            parsedCabinetAccessId = UUID.fromString(cabinetAccessId.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid cabinetAccessId: " + cabinetAccessId);
        }

        CabinetAccess cabinetAccess = cabinetAccessRepository.findById(parsedCabinetAccessId)
                .orElseThrow(() -> new IllegalArgumentException("CabinetAccess not found: " + parsedCabinetAccessId));

        if (!CabinetAccessStatus.OPEN.equals(cabinetAccess.getStatus())) {
            throw new IllegalStateException("Cannot create snapshot for closed cabinetAccess: " + parsedCabinetAccessId);
        }

        String normalizedSnapshotType = request.snapshotType() == null ? "" : request.snapshotType().trim().toUpperCase();
        if (normalizedSnapshotType.isBlank()) {
            throw new IllegalArgumentException("snapshotType is required");
        }
        if (!SnapshotType.SUPPORTED.contains(normalizedSnapshotType)) {
            throw new IllegalArgumentException("snapshotType must be BEFORE or AFTER");
        }

        if (SnapshotType.BEFORE.equals(normalizedSnapshotType)
                && inventorySnapshotRepository.existsByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, SnapshotType.BEFORE)) {
            throw new IllegalStateException("BEFORE snapshot already exists for cabinetAccess: " + parsedCabinetAccessId);
        }

        if (SnapshotType.AFTER.equals(normalizedSnapshotType)) {
            if (!inventorySnapshotRepository.existsByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, SnapshotType.BEFORE)) {
                throw new IllegalStateException("AFTER snapshot requires a previous BEFORE snapshot");
            }
            if (inventorySnapshotRepository.existsByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, SnapshotType.AFTER)) {
                throw new IllegalStateException("AFTER snapshot already exists for cabinetAccess: " + parsedCabinetAccessId);
            }
        }

        String normalizedSource = request.source() == null ? "" : request.source().trim();
        if (normalizedSource.isBlank()) {
            throw new IllegalArgumentException("source is required");
        }

        List<String> normalizedTags = Optional.ofNullable(request.observedTags())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();

        List<Tool> matchedTools = toolRepository.findByTagCodeIn(normalizedTags);

        Map<String, UUID> toolIdByTag = matchedTools.stream()
                .filter(tool -> tool.getTagCode() != null)
                .collect(Collectors.toMap(
                        tool -> tool.getTagCode().trim().toUpperCase(),
                        Tool::getId,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<String> recognizedTags = normalizedTags.stream()
                .filter(toolIdByTag::containsKey)
                .toList();

        InventorySnapshot snapshot = InventorySnapshot.newSnapshot(parsedCabinetAccessId, normalizedSnapshotType, request.capturedAt(), normalizedSource);
        InventorySnapshot savedSnapshot = inventorySnapshotRepository.save(snapshot);

        List<InventorySnapshotItem> items = recognizedTags.stream()
                .map(tag -> InventorySnapshotItem.newItem(savedSnapshot.getId(), tag, toolIdByTag.get(tag)))
                .toList();

        inventorySnapshotItemRepository.saveAll(items);

        auditService.logAction(
                "CABINET_ACCESS",
                parsedCabinetAccessId.toString(),
                "CREATE_INVENTORY_SNAPSHOT",
                AuditEntityType.INVENTORY_SNAPSHOT,
                savedSnapshot.getId()
        );


        return new CreateSnapshotResponse(savedSnapshot.getId(), recognizedTags);
    }
}

