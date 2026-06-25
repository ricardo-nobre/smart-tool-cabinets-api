package smarttoolcabinets.cabinetaccess.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.cabinetaccess.domain.CabinetAccess;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para CabinetAccess.
 */
public interface CabinetAccessRepository extends JpaRepository<CabinetAccess, UUID> {

    Optional<CabinetAccess> findFirstByCabinetIdAndStatus(UUID cabinetId, String status);
}

