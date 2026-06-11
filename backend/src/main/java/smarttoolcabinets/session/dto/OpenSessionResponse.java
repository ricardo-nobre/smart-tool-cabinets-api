package smarttoolcabinets.session.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de abertura de CabinetAccess.
 */
public record OpenSessionResponse(
        UUID cabinetAccessId,
        String status,
        OffsetDateTime openedAt
) {
}

