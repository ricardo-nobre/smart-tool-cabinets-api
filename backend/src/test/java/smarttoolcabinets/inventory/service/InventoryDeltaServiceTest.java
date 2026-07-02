package smarttoolcabinets.inventory.service;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryDeltaServiceTest {

    private final InventoryDeltaService service = new InventoryDeltaService();

    @Test
    void calculatesRemovedReturnedAndUnchangedToolIds() {
        UUID tool1 = UUID.fromString("00000000-0000-0000-0000-000000001001");
        UUID tool2 = UUID.fromString("00000000-0000-0000-0000-000000001002");
        UUID tool3 = UUID.fromString("00000000-0000-0000-0000-000000001003");
        UUID tool4 = UUID.fromString("00000000-0000-0000-0000-000000001004");

        InventoryDelta delta = service.calculate(
                Set.of(tool1, tool2, tool3),
                Set.of(tool1, tool3, tool4)
        );

        assertThat(delta.removed()).containsExactlyInAnyOrder(tool2);
        assertThat(delta.returned()).containsExactlyInAnyOrder(tool4);
        assertThat(delta.unchanged()).containsExactlyInAnyOrder(tool1, tool3);
    }
}
