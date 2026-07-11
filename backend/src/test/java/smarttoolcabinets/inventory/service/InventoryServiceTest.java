package smarttoolcabinets.inventory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.cabinetaccess.dto.OpenCabinetAccessRequest;
import smarttoolcabinets.cabinetaccess.service.DeviceCabinetAccessService;
import smarttoolcabinets.inventory.dto.CreateSnapshotRequest;
import smarttoolcabinets.tool.dto.AdminToolCreateRequest;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.tool.service.AdminToolService;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DeviceCabinetAccessService deviceCabinetAccessService;

    @Autowired
    private CabinetRepository cabinetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private AdminToolService adminToolService;

    @Test
    void createAfterSnapshotWithoutBeforeSnapshotFails() {
        TestAccess access = createOpenAccessWithTool();

        assertThatThrownBy(() -> inventoryService.createSnapshot(access.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "AFTER",
                OffsetDateTime.now(),
                "TEST",
                List.of(access.tool().getTagCode())
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AFTER snapshot requires a previous BEFORE snapshot");
    }

    @Test
    void createDuplicateBeforeSnapshotFails() {
        TestAccess access = createOpenAccessWithTool();

        var response = inventoryService.createSnapshot(access.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "BEFORE",
                OffsetDateTime.now(),
                "TEST",
                List.of(access.tool().getTagCode().toLowerCase(Locale.ROOT))
        ));
        assertThat(response.recognizedTags()).containsExactly(access.tool().getTagCode());

        assertThatThrownBy(() -> inventoryService.createSnapshot(access.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "BEFORE",
                OffsetDateTime.now(),
                "TEST",
                List.of(access.tool().getTagCode())
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BEFORE snapshot already exists");
    }

    @Test
    void createDuplicateAfterSnapshotFails() {
        TestAccess access = createOpenAccessWithTool();

        inventoryService.createSnapshot(access.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "BEFORE",
                OffsetDateTime.now(),
                "TEST",
                List.of(access.tool().getTagCode())
        ));
        inventoryService.createSnapshot(access.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "AFTER",
                OffsetDateTime.now(),
                "TEST",
                List.of()
        ));

        assertThatThrownBy(() -> inventoryService.createSnapshot(access.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "AFTER",
                OffsetDateTime.now(),
                "TEST",
                List.of()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AFTER snapshot already exists");
    }

    private TestAccess createOpenAccessWithTool() {
        String suffix = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Cabinet cabinet = cabinetRepository.save(Cabinet.newCabinet("CAB-INV-" + suffix, "Cabinet INV", "Lab"));
        User operator = userRepository.save(User.newUser("operator-inv-" + suffix, "Operator INV", UserRole.OPERATOR, User.hashPin("1234"), null));
        String lowerCaseTagCode = ("tag-inv-" + suffix).toLowerCase(Locale.ROOT);
        UUID toolId = UUID.fromString(adminToolService.createTool(new AdminToolCreateRequest(
                cabinet.getId(),
                lowerCaseTagCode,
                "Tool INV"
        )));
        Tool tool = toolRepository.findById(toolId).orElseThrow();
        assertThat(tool.getTagCode()).isEqualTo(lowerCaseTagCode.toUpperCase(Locale.ROOT));

        var opened = deviceCabinetAccessService.openCabinetAccess(new OpenCabinetAccessRequest(cabinet.getCode(), operator.getId()));
        return new TestAccess(opened.cabinetAccessId(), tool);
    }

    private record TestAccess(UUID cabinetAccessId, Tool tool) {
    }
}
