package smarttoolcabinets.operator.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OperatorQueryServiceTest {

    @Autowired
    private OperatorQueryService operatorQueryService;

    @Autowired
    private CabinetRepository cabinetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolAssignmentRepository toolAssignmentRepository;

    @Test
    void endOfDayCheckReturnsActiveAssignmentAsPending() {
        String suffix = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Cabinet cabinet = cabinetRepository.save(Cabinet.newCabinet("CAB-EOD-" + suffix, "Cabinet EOD", "Lab"));
        User operator = userRepository.save(User.newUser("operator-eod-" + suffix, "Operator EOD", "OPERATOR", "4321-" + suffix, null));
        Tool tool = toolRepository.save(Tool.newTool(cabinet.getId(), "TAG-EOD-" + suffix, "Tool EOD", 1, null));
        ToolAssignment assignment = toolAssignmentRepository.save(ToolAssignment.createActive(
                tool.getId(),
                operator.getId(),
                cabinet.getId(),
                UUID.randomUUID(),
                OffsetDateTime.now()
        ));

        var response = operatorQueryService.endOfDayCheck(operator.getId());

        assertThat(response.pendingAssignmentsCount()).isEqualTo(1);
        assertThat(response.requireSupervisorReview()).isTrue();
        assertThat(response.allowExit()).isFalse();
        assertThat(response.pendingAssignments())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.assignmentId()).isEqualTo(assignment.getId());
                    assertThat(item.status()).isEqualTo("ACTIVE");
                    assertThat(item.tagCode()).isEqualTo(tool.getTagCode());
                });
    }
}
