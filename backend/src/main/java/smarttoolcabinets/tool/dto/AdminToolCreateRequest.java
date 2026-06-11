package smarttoolcabinets.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Pedido administrativo para criar ferramenta.
 *
 * Evolucao futura: regras de unicidade por armario e validacao de formato de tag.
 */
public record AdminToolCreateRequest(
        @NotNull UUID cabinetId,
        @NotBlank String tagCode,
        @NotBlank String displayName,
        @NotNull Integer typeCode,
        String serialNumber
) {
}

