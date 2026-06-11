package smarttoolcabinets.cabinet.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Pedido administrativo para criar armario.
 *
 * Evolucao futura: regras de validacao de codigo unico e politica de naming.
 */
public record AdminCabinetCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        String location
) {
}

