package smarttoolcabinets.operator.dto;

import java.util.List;
import java.util.UUID;

public record EndOfDayCheckResponse(
        UUID operatorId,
        List<ToolAssignmentItem> pendingAssignments,
        int pendingAssignmentsCount,
        boolean requireSupervisorReview,
        boolean allowExit
) {
}

