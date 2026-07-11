package smarttoolcabinets.toolassignment.domain;

import java.util.Set;

public final class ToolAssignmentStatus {

    public static final String ACTIVE = "ACTIVE";
    public static final String RETURNED = "RETURNED";
    public static final String PENDING_REVIEW = "PENDING_REVIEW";
    public static final String RESOLVED = "RESOLVED";

    public static final Set<String> SUPPORTED = Set.of(ACTIVE, RETURNED, PENDING_REVIEW, RESOLVED);
    public static final Set<String> OPEN = Set.of(ACTIVE, PENDING_REVIEW);

    private ToolAssignmentStatus() {
    }
}
