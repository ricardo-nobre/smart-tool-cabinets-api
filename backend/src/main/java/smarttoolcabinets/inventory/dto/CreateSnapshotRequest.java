package smarttoolcabinets.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateSnapshotRequest (
        @NotBlank @Size(max = 16) String snapshotType,
        @NotNull OffsetDateTime capturedAt,
        @NotBlank @Size(max = 32) String source,
        @NotEmpty List<@NotBlank String> observedTags
        ){
}
