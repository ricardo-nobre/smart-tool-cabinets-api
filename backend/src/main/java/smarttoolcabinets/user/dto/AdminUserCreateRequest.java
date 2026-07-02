package smarttoolcabinets.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @NotBlank @Size(max = 64) String username,
        @Size(max = 128) String fullName,
        @NotBlank @Size(max = 32) String role,
        @Size(max = 255) String pin,
        @Size(max = 128) String nfcUid
) {
}

