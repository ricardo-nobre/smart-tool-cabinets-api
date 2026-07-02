package smarttoolcabinets.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Item individual de um snapshot de inventario observado por RFID.
 */
@Entity
@Table(name = "inventory_snapshot_item")
public class InventorySnapshotItem {

    @Id
    private UUID id;

    @Column(name = "snapshot_id", nullable = false)
    private UUID snapshotId;

    @Column(name = "tag_code", nullable = false)
    private String tagCode;

    @Column(name = "tool_id")
    private UUID toolId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected InventorySnapshotItem() {
        // JPA
    }

    public static InventorySnapshotItem newItem(UUID snapshotId, String tagCode, UUID toolId) {
        InventorySnapshotItem item = new InventorySnapshotItem();
        item.id = UUID.randomUUID();
        item.snapshotId = snapshotId;
        item.tagCode = tagCode;
        item.toolId = toolId;
        item.createdAt = OffsetDateTime.now();
        return item;
    }

    public String getTagCode() {
        return tagCode;
    }

    public UUID getToolId() {
        return toolId;
    }
}

