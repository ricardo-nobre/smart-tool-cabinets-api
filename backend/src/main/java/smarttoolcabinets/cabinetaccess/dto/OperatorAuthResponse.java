package smarttoolcabinets.cabinetaccess.dto;

import java.util.UUID;

/**
 * Resposta de autenticacao de operador.
 */
public record OperatorAuthResponse(
        UUID operatorId,
        String status
) {
}

