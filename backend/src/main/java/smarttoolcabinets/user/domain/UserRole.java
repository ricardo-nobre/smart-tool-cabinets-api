package smarttoolcabinets.user.domain;

import java.util.Set;

public final class UserRole {

    public static final String ADMIN = "ADMIN";
    public static final String OPERATOR = "OPERATOR";
    public static final String SUPERVISOR = "SUPERVISOR";

    public static final Set<String> SUPPORTED = Set.of(ADMIN, OPERATOR, SUPERVISOR);

    private UserRole() {
    }
}
