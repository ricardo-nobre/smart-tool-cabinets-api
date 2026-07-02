package smarttoolcabinets.inventory.domain;

import java.util.Set;

public final class SnapshotType {

    public static final String BEFORE = "BEFORE";
    public static final String AFTER = "AFTER";

    public static final Set<String> SUPPORTED = Set.of(BEFORE, AFTER);

    private SnapshotType() {
    }
}
