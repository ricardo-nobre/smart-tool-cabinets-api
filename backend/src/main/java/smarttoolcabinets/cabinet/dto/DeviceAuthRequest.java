package smarttoolcabinets.cabinet.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Pedido de autenticacao de dispositivo.
 *
 * Existe para transportar credenciais minimas do armario no endpoint de auth.
 * Evolucao futura: regras detalhadas de formato e rotacao de chave.
 */
public record DeviceAuthRequest(
        @NotBlank String cabinetCode,
        @NotBlank String apiKey
) {
}

