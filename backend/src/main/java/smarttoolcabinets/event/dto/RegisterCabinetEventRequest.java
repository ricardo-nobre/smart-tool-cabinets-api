package smarttoolcabinets.event.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pedido para registo de evento emitido por dispositivo.
 *
 * Evolucao futura: validacao por tipo de evento e consistencia temporal.
 */
public record RegisterCabinetEventRequest(
        @NotNull UUID cabinetAccessId,
        @NotBlank String eventType,
        JsonNode payload,
        @NotNull OffsetDateTime occurredAt
) {
}

