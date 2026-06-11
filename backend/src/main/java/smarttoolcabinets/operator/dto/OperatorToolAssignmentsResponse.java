package smarttoolcabinets.operator.dto;

import java.util.List;
import java.util.UUID;

public record OperatorToolAssignmentsResponse(
        UUID operatorId,
        List<ToolAssignmentItem> assignments
) {
}

