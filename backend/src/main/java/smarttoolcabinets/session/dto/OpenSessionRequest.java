package smarttoolcabinets.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Pedido para abrir CabinetAccess no armario.
 */
public record OpenSessionRequest(
        @NotBlank String cabinetCode,
        @NotNull UUID operatorId
) {
}

