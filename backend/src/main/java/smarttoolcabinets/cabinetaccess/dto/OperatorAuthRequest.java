package smarttoolcabinets.cabinetaccess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Pedido de autenticacao de operador no contexto do armario.
 */
public record OperatorAuthRequest(
        @NotBlank @Size(max = 64) String cabinetCode,
        @NotNull Method method,
        @NotBlank @Size(max = 255) String credential
) {
    public enum Method {
        PIN,
        NFC
    }
}

