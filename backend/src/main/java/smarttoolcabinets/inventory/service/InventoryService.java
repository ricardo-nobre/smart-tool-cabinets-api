package smarttoolcabinets.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.inventory.domain.InventorySnapshot;
import smarttoolcabinets.inventory.domain.InventorySnapshotItem;
import smarttoolcabinets.inventory.dto.CreateSnapshotRequest;
import smarttoolcabinets.inventory.dto.CreateSnapshotResponse;
import smarttoolcabinets.inventory.repository.InventorySnapshotItemRepository;
import smarttoolcabinets.inventory.repository.InventorySnapshotRepository;
import smarttoolcabinets.cabinetaccess.domain.CabinetAccess;
import smarttoolcabinets.cabinetaccess.repository.CabinetAccessRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;

import java.util.*;
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

    /**
     * Objetivo: guardar snapshot de inventario associado a CabinetAccess.
     * Inputs esperados: cabinetAccessId, origem do snapshot e lista de tags observadas.
     * Output esperado: id do snapshot criado.
     * Passos logicos a implementar:
     * 1) Validar CabinetAccess e estrutura do payload.
     * 2) Persistir snapshot cabecalho.
     * 3) Persistir itens reconhecidos/desconhecidos por tag.
     * 4) Produzir informacao para analise de delta no close.
     * Notas: assegurar consistencia transacional snapshot + itens.
     */


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

        if (!"OPEN".equals(cabinetAccess.getStatus())) {
            throw new IllegalStateException("Cannot create snapshot for closed cabinetAccess: " + parsedCabinetAccessId);
        }

        String normalizedSnapshotType = request.snapshotType() == null ? "" : request.snapshotType().trim().toUpperCase();
        if (normalizedSnapshotType.isBlank()) {
            throw new IllegalArgumentException("snapshotType is required");
        }
        if (!Set.of("BEFORE", "AFTER", "EXTRA").contains(normalizedSnapshotType)) {
            throw new IllegalArgumentException("snapshotType must be BEFORE, AFTER or EXTRA");
        }

        if ("BEFORE".equals(normalizedSnapshotType)
                && inventorySnapshotRepository.existsByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, "BEFORE")) {
            throw new IllegalStateException("BEFORE snapshot already exists for cabinetAccess: " + parsedCabinetAccessId);
        }

        if ("AFTER".equals(normalizedSnapshotType)) {
            if (!inventorySnapshotRepository.existsByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, "BEFORE")) {
                throw new IllegalStateException("AFTER snapshot requires a previous BEFORE snapshot");
            }
            if (inventorySnapshotRepository.existsByCabinetAccessIdAndSnapshotType(parsedCabinetAccessId, "AFTER")) {
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

        if (normalizedTags.isEmpty()) {
            throw new IllegalArgumentException("observedTags must contain at least one non-blank tag");
        }

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


        List<String> unknownTags = normalizedTags.stream()
                .filter(tag -> !toolIdByTag.containsKey(tag))
                .toList();

        InventorySnapshot is = InventorySnapshot.newSnapshot(parsedCabinetAccessId, normalizedSnapshotType, request.capturedAt(), normalizedSource);
        InventorySnapshot i = inventorySnapshotRepository.save(is);

        List<InventorySnapshotItem> items = normalizedTags.stream()
                        .map(tag -> {
                            UUID toolId = toolIdByTag.get(tag);
                            boolean recognized = toolId != null;
                            return InventorySnapshotItem.newItem(i.getId(), tag, toolId, recognized);
                        })
                        .toList();

        inventorySnapshotItemRepository.saveAll(items);

        auditService.logAction(
                "CABINET_ACCESS:" + parsedCabinetAccessId,
                "CREATE_INVENTORY_SNAPSHOT",
                AuditEntityType.INVENTORY_SNAPSHOT,
                i.getId().toString()
        );


        return new CreateSnapshotResponse(i.getId(), recognizedTags, unknownTags);
    }
}



