package smarttoolcabinets.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateSnapshotRequest (
        @NotBlank String snapshotType,
        @NotNull OffsetDateTime capturedAt,
        @NotBlank String source,
        @NotEmpty List<@NotBlank String> observedTags
        ){
}
