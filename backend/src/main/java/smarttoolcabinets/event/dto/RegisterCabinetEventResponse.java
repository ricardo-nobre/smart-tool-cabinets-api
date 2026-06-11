package smarttoolcabinets.event.dto;

import java.util.UUID;

/**
 * Resposta minima do endpoint de registo de evento.
 *
 * Evolucao futura: metadados adicionais necessarios para observabilidade.
 */
public record RegisterCabinetEventResponse(
        UUID eventId,
        String status
) {
}

