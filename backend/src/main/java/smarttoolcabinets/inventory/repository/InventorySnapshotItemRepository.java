package smarttoolcabinets.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.inventory.domain.InventorySnapshotItem;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para InventorySnapshotItem.
 *
 * Existe para gerir o detalhe de cada ferramenta por snapshot.
 * Evolucao futura: consultas para deteccao de ferramentas em falta.
 */
public interface InventorySnapshotItemRepository extends JpaRepository<InventorySnapshotItem, UUID> {

    List<InventorySnapshotItem> findBySnapshotId(UUID snapshotId);
}

