package smarttoolcabinets.inventory.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class InventoryDeltaService {

    public <T> InventoryDelta<T> calculate(Set<T> before, Set<T> after) {
        Set<T> normalizedBefore = copyOf(before);
        Set<T> normalizedAfter = copyOf(after);

        Set<T> removed = new LinkedHashSet<>(normalizedBefore);
        removed.removeAll(normalizedAfter);

        Set<T> returned = new LinkedHashSet<>(normalizedAfter);
        returned.removeAll(normalizedBefore);

        Set<T> unchanged = new LinkedHashSet<>(normalizedBefore);
        unchanged.retainAll(normalizedAfter);

        return new InventoryDelta<>(
                Set.copyOf(removed),
                Set.copyOf(returned),
                Set.copyOf(unchanged)
        );
    }

    private <T> Set<T> copyOf(Set<T> values) {
        return values == null ? Set.of() : new LinkedHashSet<>(values);
    }
}
