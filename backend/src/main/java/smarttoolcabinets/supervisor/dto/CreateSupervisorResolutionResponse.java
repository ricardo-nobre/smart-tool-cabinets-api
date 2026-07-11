package smarttoolcabinets.supervisor.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateSupervisorResolutionResponse(
        UUID resolutionId,
        UUID operatorId,
        UUID supervisorId,
        OffsetDateTime decisionAt,
        String reasonCode,
        String reportText,
        UUID resolvedAssignmentId
) {
}
