package smarttoolcabinets.supervisor.domain;

import java.util.Locale;
import java.util.Set;

public final class SupervisorResolutionReasonCode {

    public static final String TOOL_LOST = "TOOL_LOST";
    public static final String TOOL_DAMAGED = "TOOL_DAMAGED";
    public static final String RFID_FAILURE = "RFID_FAILURE";
    public static final String MANUAL_VERIFICATION = "MANUAL_VERIFICATION";
    public static final String OTHER = "OTHER";

    public static final Set<String> SUPPORTED = Set.of(
            TOOL_LOST,
            TOOL_DAMAGED,
            RFID_FAILURE,
            MANUAL_VERIFICATION,
            OTHER
    );

    private SupervisorResolutionReasonCode() {
    }

    public static String normalize(String reasonCode) {
        return reasonCode == null ? "" : reasonCode.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean deactivatesTool(String reasonCode) {
        return TOOL_LOST.equals(reasonCode)
                || TOOL_DAMAGED.equals(reasonCode)
                || RFID_FAILURE.equals(reasonCode);
    }
}
