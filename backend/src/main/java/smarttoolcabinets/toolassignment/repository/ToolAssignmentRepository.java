package smarttoolcabinets.toolassignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio base para consultas de custodia por operador.
 */
public interface ToolAssignmentRepository extends JpaRepository<ToolAssignment, UUID> {

	List<ToolAssignment> findByOperatorId(UUID operatorId);

	List<ToolAssignment> findByOperatorIdAndStatus(UUID operatorId, String status);

	Optional<ToolAssignment> findByToolIdAndStatus(UUID toolId, String status);
}

