package smarttoolcabinets.operator.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ToolAssignmentItem(
        UUID assignmentId,
        UUID toolId,
        String tagCode,
        String toolDisplayName,
        String originCabinetCode,
        OffsetDateTime assignedAt,
        OffsetDateTime returnedAt,
        String status
) {
}

