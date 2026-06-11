package smarttoolcabinets.supervisor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.supervisor.domain.SupervisorResolution;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio base para resolucoes de supervisor.
 */
public interface SupervisorResolutionRepository extends JpaRepository<SupervisorResolution, UUID> {

	List<SupervisorResolution> findByOperatorId(UUID operatorId);
}

