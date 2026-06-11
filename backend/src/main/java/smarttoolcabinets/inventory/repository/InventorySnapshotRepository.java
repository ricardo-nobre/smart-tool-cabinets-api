package smarttoolcabinets.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.inventory.domain.InventorySnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para InventorySnapshot.
 *
 * Serve para aceder ao estado consolidado de inventario por cabinet access.
 */
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, UUID> {

    List<InventorySnapshot> findByCabinetAccessId(UUID cabinetAccessId);

    List<InventorySnapshot> findByCabinetAccessIdAndSnapshotType(UUID cabinetAccessId, String snapshotType);

    boolean existsByCabinetAccessIdAndSnapshotType(UUID cabinetAccessId, String snapshotType);

    Optional<InventorySnapshot> findTopByCabinetAccessIdOrderByCapturedAtDesc(UUID cabinetAccessId);
}

