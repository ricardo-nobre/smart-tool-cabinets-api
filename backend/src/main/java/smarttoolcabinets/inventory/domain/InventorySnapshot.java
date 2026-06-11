package smarttoolcabinets.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade de snapshot de inventario num momento do CabinetAccess.
 */
@Entity
@Table(name = "inventory_snapshot")
public class InventorySnapshot {

    @Id
    private UUID id;

    @Column(name = "cabinet_access_id", nullable = false)
    private UUID cabinetAccessId;

    @Column(name = "snapshot_type", nullable = false, length = 16)
    private String snapshotType;

    @Column(name = "captured_at", nullable = false)
    private OffsetDateTime capturedAt;

    @Column(nullable = false, length = 32)
    private String source;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected InventorySnapshot() {
        // JPA
    }

    public static InventorySnapshot newSnapshot(UUID cabinetAccessId, String snapshotType, OffsetDateTime capturedAt, String source) {
        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.id = UUID.randomUUID();
        snapshot.cabinetAccessId = cabinetAccessId;
        snapshot.snapshotType = snapshotType;
        snapshot.capturedAt = capturedAt;
        snapshot.source = source;
        snapshot.createdAt = OffsetDateTime.now();
        return snapshot;
    }

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCapturedAt() {
        return capturedAt;
    }

    public String getSource() {
        return source;
    }

    public String getSnapshotType() {
        return snapshotType;
    }

    public UUID getCabinetAccessId() {
        return cabinetAccessId;
    }


}

