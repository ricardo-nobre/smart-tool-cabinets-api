package smarttoolcabinets.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Pedido administrativo para criar utilizador.
 *
 * Evolucao futura: regras de role e validacao de username/politica de acessos.
 */
public record AdminUserCreateRequest(
        @NotBlank String username,
        String fullName,
        @NotBlank String role,
        String pin,
        String nfcUid
) {
}

