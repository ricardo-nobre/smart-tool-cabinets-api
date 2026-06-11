package smarttoolcabinets.supervisor.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SupervisorResolutionAssignmentId implements Serializable {

    @Column(name = "supervisor_resolution_id", nullable = false)
    private UUID supervisorResolutionId;

    @Column(name = "tool_assignment_id", nullable = false)
    private UUID toolAssignmentId;

    protected SupervisorResolutionAssignmentId() {
    }

    public SupervisorResolutionAssignmentId(UUID supervisorResolutionId, UUID toolAssignmentId) {
        this.supervisorResolutionId = supervisorResolutionId;
        this.toolAssignmentId = toolAssignmentId;
    }

    public UUID getSupervisorResolutionId() {
        return supervisorResolutionId;
    }

    public UUID getToolAssignmentId() {
        return toolAssignmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SupervisorResolutionAssignmentId that)) {
            return false;
        }
        return Objects.equals(supervisorResolutionId, that.supervisorResolutionId)
                && Objects.equals(toolAssignmentId, that.toolAssignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supervisorResolutionId, toolAssignmentId);
    }
}

