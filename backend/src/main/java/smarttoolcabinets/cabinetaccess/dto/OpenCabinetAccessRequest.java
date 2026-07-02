package smarttoolcabinets.cabinetaccess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Pedido para abrir CabinetAccess no armario.
 */
public record OpenCabinetAccessRequest(
        @NotBlank @Size(max = 64) String cabinetCode,
        @NotNull UUID operatorId
) {
}

