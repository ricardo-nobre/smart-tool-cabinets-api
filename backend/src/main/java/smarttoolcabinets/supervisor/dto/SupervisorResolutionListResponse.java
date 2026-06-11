package smarttoolcabinets.supervisor.dto;

import java.util.List;

public record SupervisorResolutionListResponse(
        List<CreateSupervisorResolutionResponse> items
) {
}

