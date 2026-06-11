package smarttoolcabinets.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade de evento emitido pelo dispositivo durante um CabinetAccess.
 */
@Entity
@Table(name = "cabinet_event")
public class CabinetEvent {

    @Id
    private UUID id;

    @Column(name = "cabinet_access_id", nullable = false)
    private UUID cabinetAccessId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected CabinetEvent() {

    }

    public static CabinetEvent cabinetEvent(UUID cabinetAccessId, String eventType, String payloadJson, OffsetDateTime occurredAt) {
            CabinetEvent event = new CabinetEvent();
            event.id = UUID.randomUUID();
            event.cabinetAccessId = cabinetAccessId;
            event.eventType = eventType;
            event.payloadJson = payloadJson;
            event.occurredAt = occurredAt;
            event.createdAt = OffsetDateTime.now();
            return event;
    }

    public UUID getId() {
        return id;
    }
}

