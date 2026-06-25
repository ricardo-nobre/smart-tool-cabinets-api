package smarttoolcabinets.session.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.inventory.dto.CreateSnapshotRequest;
import smarttoolcabinets.inventory.service.InventoryService;
import smarttoolcabinets.session.dto.OpenSessionRequest;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DeviceSessionServiceTest {

    @Autowired
    private DeviceSessionService deviceSessionService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CabinetRepository cabinetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolAssignmentRepository toolAssignmentRepository;

    @Test
    void closeCreatesActiveAssignmentWhenToolDisappearsBetweenBeforeAndAfterSnapshots() {
        String suffix = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Cabinet cabinet = cabinetRepository.save(Cabinet.newCabinet("CAB-" + suffix, "Cabinet " + suffix, "Lab"));
        User operator = userRepository.save(User.newUser("operator-" + suffix, "Operator Test", "OPERATOR", "1234-" + suffix, null));
        Tool toolA = toolRepository.save(Tool.newTool(cabinet.getId(), "TAG-A-" + suffix, "Tool A", 1, null));
        Tool toolB = toolRepository.save(Tool.newTool(cabinet.getId(), "TAG-B-" + suffix, "Tool B", 1, null));

        var opened = deviceSessionService.openSession(new OpenSessionRequest(cabinet.getCode(), operator.getId()));

        inventoryService.createSnapshot(opened.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "BEFORE",
                OffsetDateTime.now(),
                "TEST",
                List.of(toolA.getTagCode(), toolB.getTagCode())
        ));
        inventoryService.createSnapshot(opened.cabinetAccessId().toString(), new CreateSnapshotRequest(
                "AFTER",
                OffsetDateTime.now(),
                "TEST",
                List.of(toolA.getTagCode())
        ));

        var closed = deviceSessionService.closeSession(opened.cabinetAccessId().toString());

        assertThat(closed.operationalResult()).isEqualTo("CLOSED_WITH_ASSIGNMENTS");
        assertThat(closed.assignmentsCreatedCount()).isEqualTo(1);
        assertThat(closed.discrepancyFlag()).isFalse();

        var assignment = toolAssignmentRepository.findByToolIdAndStatus(toolB.getId(), "ACTIVE");
        assertThat(assignment).isPresent();
        assertThat(assignment.get().getOperatorId()).isEqualTo(operator.getId());
    }
}
