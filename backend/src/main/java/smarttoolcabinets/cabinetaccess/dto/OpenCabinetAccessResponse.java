package smarttoolcabinets.cabinetaccess.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de abertura de CabinetAccess.
 */
public record OpenCabinetAccessResponse(
        UUID cabinetAccessId,
        String status,
        OffsetDateTime openedAt
) {
}

