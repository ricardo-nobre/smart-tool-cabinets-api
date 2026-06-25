package smarttoolcabinets.inventory.service;

import java.util.Set;

public record InventoryDelta<T>(
        Set<T> removed,
        Set<T> returned,
        Set<T> unchanged
) {
}
