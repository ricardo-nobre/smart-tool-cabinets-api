package smarttoolcabinets.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.inventory.domain.InventorySnapshotItem;

import java.util.List;
import java.util.UUID;

public interface InventorySnapshotItemRepository extends JpaRepository<InventorySnapshotItem, UUID> {

    List<InventorySnapshotItem> findBySnapshotId(UUID snapshotId);
}

