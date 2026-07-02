package smarttoolcabinets.tool.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AdminToolCreateRequest(
        @NotNull UUID cabinetId,
        @NotBlank @Size(max = 128) String tagCode,
        @NotBlank @Size(max = 128) String displayName
) {
}

