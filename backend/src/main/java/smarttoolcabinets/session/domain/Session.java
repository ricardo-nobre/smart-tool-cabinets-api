package smarttoolcabinets.session.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade de acesso operacional ao armario (CabinetAccess).
 *
 * Mantemos o nome de classe `Session` apenas por compatibilidade interna,
 * mas o contrato e o schema oficiais usam o conceito `CabinetAccess`.
 */
@Entity
@Table(name = "cabinet_access")
public class Session {

    @Id
    private UUID id;

    @Column(name = "cabinet_id", nullable = false)
    private UUID cabinetId;

    @Column(name = "operator_id", nullable = false)
    private UUID operatorId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Session() {

    }

    public static Session open(UUID cabinetId, UUID operatorId) {
        Session session = new Session();
        session.id = UUID.randomUUID();
        session.cabinetId = cabinetId;
        session.operatorId = operatorId;
        session.status = "OPEN";
        session.openedAt = OffsetDateTime.now();
        session.closedAt = null;
        session.createdAt = OffsetDateTime.now();
        return session;
    }

    public UUID getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void close() {
        this.status = "CLOSED";
        this.closedAt = OffsetDateTime.now();
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public OffsetDateTime getOpenedAt() {
        return openedAt;
    }

    public UUID getCabinetId() {
        return cabinetId;
    }

    public UUID getOperatorId() {
        return operatorId;
    }
}

