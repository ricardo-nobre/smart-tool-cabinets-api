package smarttoolcabinets.inventory.service;

import java.util.Set;
import java.util.UUID;

public record InventoryDelta(
        Set<UUID> removed,
        Set<UUID> returned,
        Set<UUID> unchanged
) {
}
