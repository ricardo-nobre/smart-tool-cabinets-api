package smarttoolcabinets.inventory.dto;

import java.util.List;
import java.util.UUID;

public record CreateSnapshotResponse (
        UUID snapshotId,
        List<String> recognizedTags,
        List<String> unknownTags
) {
}
