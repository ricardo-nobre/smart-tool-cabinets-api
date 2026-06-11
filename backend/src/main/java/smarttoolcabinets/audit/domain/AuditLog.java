package smarttoolcabinets.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de auditoria transversal do sistema.
 *
 * Regista quem fez o que e quando, para rastreabilidade.
 * Relaciona-se com fluxo de CabinetAccess/eventos e operacoes administrativas.
 * Evolucao futura: estrategia de detalhes e padroes de acao.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    private UUID id;

    @Column(name = "actor_type", nullable = false, length = 32)
    private String actorType;

    @Column(name = "actor_ref", length = 128)
    private String actorRef;

    @Column(nullable = false, length = 128)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 64)
    private AuditEntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AuditLog() {
        // JPA
    }

    public static AuditLog newEntry(String actorType, String actorRef, String action, AuditEntityType entityType, UUID entityId, String detailsJson) {
        AuditLog auditLog = new AuditLog();
        auditLog.id = UUID.randomUUID();
        auditLog.actorType = Objects.requireNonNull(actorType, "actorType is required");
        auditLog.actorRef = actorRef;
        auditLog.action = Objects.requireNonNull(action, "action is required");
        auditLog.entityType = Objects.requireNonNull(entityType, "entityType is required");
        auditLog.entityId = entityId;
        auditLog.detailsJson = detailsJson;
        auditLog.createdAt = OffsetDateTime.now();
        return auditLog;
    }
}
