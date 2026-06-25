package smarttoolcabinets.inventory.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryDeltaServiceTest {

    private final InventoryDeltaService service = new InventoryDeltaService();

    @Test
    void calculatesRemovedReturnedAndUnchangedTags() {
        InventoryDelta<String> delta = service.calculate(
                Set.of("TAG-001", "TAG-002", "TAG-003"),
                Set.of("TAG-001", "TAG-003", "TAG-004")
        );

        assertThat(delta.removed()).containsExactlyInAnyOrder("TAG-002");
        assertThat(delta.returned()).containsExactlyInAnyOrder("TAG-004");
        assertThat(delta.unchanged()).containsExactlyInAnyOrder("TAG-001", "TAG-003");
    }
}
