package smarttoolcabinets.cabinet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceAuthRequest(
        @NotBlank @Size(max = 64) String cabinetCode,
        @NotBlank @Size(max = 255) String apiKey
) {
}

