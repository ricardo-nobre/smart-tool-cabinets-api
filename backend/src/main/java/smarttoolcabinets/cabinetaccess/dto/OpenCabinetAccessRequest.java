package smarttoolcabinets.cabinetaccess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Pedido para abrir CabinetAccess no armario.
 */
public record OpenCabinetAccessRequest(
        @NotBlank String cabinetCode,
        @NotNull UUID operatorId
) {
}

