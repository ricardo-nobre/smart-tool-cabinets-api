package smarttoolcabinets.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Pedido de autenticacao de operador no contexto do armario.
 */
public record OperatorAuthRequest(
        @NotBlank String cabinetCode,
        @NotNull Method method,
        @NotBlank String credential
) {
    public enum Method {
        PIN,
        NFC
    }
}

