package smarttoolcabinets.cabinet.dto;

import java.time.OffsetDateTime;

/**
 * Resposta de autenticacao de dispositivo.
 */
public record DeviceAuthResponse(
        String deviceToken,
        OffsetDateTime expiresAt
) {
}

