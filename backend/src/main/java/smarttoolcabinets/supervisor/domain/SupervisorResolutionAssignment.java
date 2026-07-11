package smarttoolcabinets.supervisor.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Associação entre uma SupervisorResolution e uma ToolAssignment.
 * A V4 impõe unicidade nos dois identificadores.
 */
@Entity
@Table(name = "supervisor_resolution_assignment")
public class SupervisorResolutionAssignment {

    @EmbeddedId
    private SupervisorResolutionAssignmentId id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SupervisorResolutionAssignment() {
        // JPA
    }

    public static SupervisorResolutionAssignment create(SupervisorResolutionAssignmentId id) {
        SupervisorResolutionAssignment assignment = new SupervisorResolutionAssignment();
        assignment.id = id;
        assignment.createdAt = OffsetDateTime.now();
        return assignment;
    }

    public SupervisorResolutionAssignmentId getId() {
        return id;
    }
}
