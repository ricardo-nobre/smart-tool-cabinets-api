package smarttoolcabinets.supervisor.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Estrutura base da decisao formal de supervisor para pendencias.
 */
@Entity
@Table(name = "supervisor_resolution")
public class SupervisorResolution {

    @Id
    private UUID id;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(name = "supervisor_id", nullable = false)
    private UUID supervisorId;

    @Column(name = "reason_code", nullable = false, length = 64)
    private String reasonCode;

    @Column(name = "report_text", nullable = false, columnDefinition = "TEXT")
    private String reportText;

    @Column(name = "decision_at", nullable = false)
    private OffsetDateTime decisionAt;

    @Column(name = "allow_exit", nullable = false)
    private boolean allowExit;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SupervisorResolution() {
        // JPA
    }

    public static SupervisorResolution create(
            UUID operatorId,
            UUID supervisorId,
            String reasonCode,
            String reportText,
            OffsetDateTime decisionAt,
            boolean allowExit
    ) {
        SupervisorResolution resolution = new SupervisorResolution();
        resolution.id = UUID.randomUUID();
        resolution.operatorId = operatorId;
        resolution.supervisorId = supervisorId;
        resolution.reasonCode = reasonCode;
        resolution.reportText = reportText;
        resolution.decisionAt = decisionAt;
        resolution.allowExit = allowExit;
        resolution.createdAt = OffsetDateTime.now();
        return resolution;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOperatorId() {
        return operatorId;
    }

    public UUID getSupervisorId() {
        return supervisorId;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getReportText() {
        return reportText;
    }

    public OffsetDateTime getDecisionAt() {
        return decisionAt;
    }

    public boolean isAllowExit() {
        return allowExit;
    }
}

