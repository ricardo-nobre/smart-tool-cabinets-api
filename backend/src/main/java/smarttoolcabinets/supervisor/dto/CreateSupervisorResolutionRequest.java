package smarttoolcabinets.supervisor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CreateSupervisorResolutionRequest(
        @NotNull UUID operatorId,
        @NotNull UUID supervisorId,
        @NotBlank String reasonCode,
        @NotBlank String reportText,
        @NotNull OffsetDateTime decisionAt,
        @NotNull Boolean allowExit,
        @NotEmpty List<@NotNull UUID> assignmentIds
) {
}

