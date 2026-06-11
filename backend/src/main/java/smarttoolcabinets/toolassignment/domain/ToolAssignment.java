package smarttoolcabinets.toolassignment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Estrutura base da entidade de custodia por operador.
 */
@Entity
@Table(name = "tool_assignment")
public class ToolAssignment {

    @Id
    private UUID id;

    @Column(name = "tool_id", nullable = false)
    private UUID toolId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "origin_cabinet_id", nullable = false)
    private UUID originCabinetId;

    @Column(name = "origin_cabinet_access_id", nullable = false)
    private UUID originCabinetAccessId;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    @Column(name = "returned_to_cabinet_id")
    private UUID returnedToCabinetId;

    @Column(name = "returned_via_cabinet_access_id")
    private UUID returnedViaCabinetAccessId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "pending_end_of_day", nullable = false)
    private boolean pendingEndOfDay;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ToolAssignment() {
        // JPA
    }

    public static ToolAssignment createActive(
            UUID toolId,
            UUID operatorId,
            UUID originCabinetId,
            UUID originCabinetAccessId,
            OffsetDateTime assignedAt
    ) {
        ToolAssignment assignment = new ToolAssignment();
        assignment.id = UUID.randomUUID();
        assignment.toolId = toolId;
        assignment.operatorId = operatorId;
        assignment.originCabinetId = originCabinetId;
        assignment.originCabinetAccessId = originCabinetAccessId;
        assignment.assignedAt = assignedAt;
        assignment.status = "ACTIVE";
        assignment.pendingEndOfDay = false;
        assignment.createdAt = OffsetDateTime.now();
        return assignment;
    }

    public UUID getId() {
        return id;
    }

    public UUID getToolId() {
        return toolId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public UUID getOriginCabinetId() {
        return originCabinetId;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public OffsetDateTime getReturnedAt() {
        return returnedAt;
    }

    public String getStatus() {
        return status;
    }

    public void markReturned(UUID returnedToCabinetId, UUID returnedViaCabinetAccessId, OffsetDateTime returnedAt) {
        this.returnedToCabinetId = returnedToCabinetId;
        this.returnedViaCabinetAccessId = returnedViaCabinetAccessId;
        this.returnedAt = returnedAt;
        this.status = "RETURNED";
        this.pendingEndOfDay = false;
    }

    public void markPendingReview() {
        this.status = "PENDING_REVIEW";
        this.pendingEndOfDay = true;
    }

    public void markResolved() {
        this.status = "RESOLVED";
        this.pendingEndOfDay = false;
    }
}

