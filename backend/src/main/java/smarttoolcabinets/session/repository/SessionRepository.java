package smarttoolcabinets.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.session.domain.Session;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para CabinetAccess (classe Session por compatibilidade interna).
 */
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findFirstByCabinetIdAndStatus(UUID cabinetId, String status);
}

