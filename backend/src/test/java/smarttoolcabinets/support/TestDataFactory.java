package smarttoolcabinets.support;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static UUID deterministicUuid(int suffix) {
        String tail = String.format("%012d", Math.max(0, suffix));
        return UUID.fromString("00000000-0000-0000-0000-" + tail);
    }

    public static OffsetDateTime baseTime() {
        return OffsetDateTime.parse("2026-01-01T10:00:00Z");
    }
}

