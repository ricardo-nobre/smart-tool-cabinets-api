package smarttoolcabinets.supervisor.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionRequest;
import smarttoolcabinets.supervisor.repository.SupervisorResolutionAssignmentRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.domain.ToolAssignmentStatus;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SupervisorResolutionServiceTest {

    @Autowired
    private SupervisorResolutionService supervisorResolutionService;

    @Autowired
    private SupervisorResolutionAssignmentRepository supervisorResolutionAssignmentRepository;

    @Autowired
    private CabinetRepository cabinetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private ToolAssignmentRepository toolAssignmentRepository;

    @Test
    void createResolutionMarksActiveAssignmentAsResolved() {
        String suffix = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Cabinet cabinet = cabinetRepository.save(Cabinet.newCabinet("CAB-SUP-" + suffix, "Cabinet SUP", "Lab"));
        User operator = userRepository.save(User.newUser("operator-sup-" + suffix, "Operator SUP", UserRole.OPERATOR, User.hashPin("1234"), null));
        User supervisor = userRepository.save(User.newUser("supervisor-sup-" + suffix, "Supervisor SUP", UserRole.SUPERVISOR, null, null));
        Tool tool = toolRepository.save(Tool.newTool(cabinet.getId(), "TAG-SUP-" + suffix, "Tool SUP"));
        ToolAssignment assignment = toolAssignmentRepository.save(ToolAssignment.createActive(
                tool.getId(),
                operator.getId(),
                cabinet.getId(),
                UUID.randomUUID(),
                OffsetDateTime.now()
        ));

        var response = supervisorResolutionService.create(new CreateSupervisorResolutionRequest(
                operator.getId(),
                supervisor.getId(),
                "BROKEN_TOOL",
                "Tool damaged during work.",
                OffsetDateTime.now(),
                true,
                List.of(assignment.getId())
        ));

        assertThat(response.resolvedAssignmentIds()).containsExactly(assignment.getId());
        assertThat(response.allowExit()).isTrue();

        ToolAssignment resolved = toolAssignmentRepository.findById(assignment.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);

        var links = supervisorResolutionAssignmentRepository.findByIdSupervisorResolutionId(response.resolutionId());
        assertThat(links)
                .singleElement()
                .satisfies(link -> assertThat(link.getId().getToolAssignmentId()).isEqualTo(assignment.getId()));
    }
}
