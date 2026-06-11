package smarttoolcabinets.supervisor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionAssignment;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionAssignmentId;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio da tabela de ligacao entre resolucao e assignments.
 */
public interface SupervisorResolutionAssignmentRepository extends JpaRepository<SupervisorResolutionAssignment, SupervisorResolutionAssignmentId> {

	List<SupervisorResolutionAssignment> findByIdSupervisorResolutionId(UUID supervisorResolutionId);
}

