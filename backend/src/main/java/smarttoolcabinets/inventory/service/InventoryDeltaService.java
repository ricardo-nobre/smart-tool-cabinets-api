package smarttoolcabinets.inventory.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class InventoryDeltaService {

    public InventoryDelta calculate(Set<UUID> beforeToolIds, Set<UUID> afterToolIds) {
        Set<UUID> normalizedBefore = copyOf(beforeToolIds);
        Set<UUID> normalizedAfter = copyOf(afterToolIds);

        Set<UUID> removed = new LinkedHashSet<>(normalizedBefore);
        removed.removeAll(normalizedAfter);

        Set<UUID> returned = new LinkedHashSet<>(normalizedAfter);
        returned.removeAll(normalizedBefore);

        Set<UUID> unchanged = new LinkedHashSet<>(normalizedBefore);
        unchanged.retainAll(normalizedAfter);

        return new InventoryDelta(
                Set.copyOf(removed),
                Set.copyOf(returned),
                Set.copyOf(unchanged)
        );
    }

    private Set<UUID> copyOf(Set<UUID> values) {
        return values == null ? Set.of() : new LinkedHashSet<>(values);
    }
}
