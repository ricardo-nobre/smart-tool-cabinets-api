package smarttoolcabinets.supervisor.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.operator.service.OperatorQueryService;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionRequest;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SupervisorResolutionServiceTest {

    @Autowired
    private SupervisorResolutionService supervisorResolutionService;

    @Autowired
    private OperatorQueryService operatorQueryService;

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
    void resolutionOfAllPendingAssignmentsAllowsExitOnNextEndOfDayCheck() {
        TestContext context = createContext("ALL");
        ToolAssignment assignment = createActiveAssignment(context, "A");

        var beforeResolution = operatorQueryService.endOfDayCheck(context.operator.getId());
        assertThat(beforeResolution.pendingAssignmentsCount()).isEqualTo(1);
        assertThat(beforeResolution.requireSupervisorReview()).isTrue();
        assertThat(beforeResolution.allowExit()).isFalse();

        var response = supervisorResolutionService.create(resolutionRequest(context, List.of(assignment.getId())));

        assertThat(response.resolvedAssignmentIds()).containsExactly(assignment.getId());
        ToolAssignment resolved = toolAssignmentRepository.findById(assignment.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);

        var afterResolution = operatorQueryService.endOfDayCheck(context.operator.getId());
        assertThat(afterResolution.pendingAssignmentsCount()).isZero();
        assertThat(afterResolution.requireSupervisorReview()).isFalse();
        assertThat(afterResolution.allowExit()).isTrue();

        var links = supervisorResolutionAssignmentRepository.findByIdSupervisorResolutionId(response.resolutionId());
        assertThat(links)
                .singleElement()
                .satisfies(link -> assertThat(link.getId().getToolAssignmentId()).isEqualTo(assignment.getId()));
    }

    @Test
    void partialResolutionKeepsExitBlockedWhileAnotherAssignmentIsPending() {
        TestContext context = createContext("PARTIAL");
        ToolAssignment resolvedAssignment = createActiveAssignment(context, "A");
        ToolAssignment stillPendingAssignment = createActiveAssignment(context, "B");

        supervisorResolutionService.create(resolutionRequest(context, List.of(resolvedAssignment.getId())));

        ToolAssignment resolved = toolAssignmentRepository.findById(resolvedAssignment.getId()).orElseThrow();
        ToolAssignment stillPending = toolAssignmentRepository.findById(stillPendingAssignment.getId()).orElseThrow();

        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);
        assertThat(stillPending.getStatus()).isEqualTo(ToolAssignmentStatus.ACTIVE);

        var endOfDay = operatorQueryService.endOfDayCheck(context.operator.getId());
        assertThat(endOfDay.pendingAssignmentsCount()).isEqualTo(1);
        assertThat(endOfDay.requireSupervisorReview()).isTrue();
        assertThat(endOfDay.allowExit()).isFalse();
        assertThat(endOfDay.pendingAssignments())
                .singleElement()
                .satisfies(item -> assertThat(item.assignmentId()).isEqualTo(stillPendingAssignment.getId()));
    }

    @Test
    void pendingReviewAssignmentCanBeResolved() {
        TestContext context = createContext("PENDING");
        ToolAssignment assignment = createActiveAssignment(context, "A");
        assignment.markPendingReview();
        toolAssignmentRepository.save(assignment);

        supervisorResolutionService.create(resolutionRequest(context, List.of(assignment.getId())));

        ToolAssignment resolved = toolAssignmentRepository.findById(assignment.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);
    }

    @Test
    void returnedAssignmentCannotBeResolved() {
        TestContext context = createContext("RETURNED");
        ToolAssignment assignment = createActiveAssignment(context, "A");
        assignment.markReturned(context.cabinet.getId(), UUID.randomUUID(), OffsetDateTime.now());
        toolAssignmentRepository.save(assignment);

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(context, List.of(assignment.getId()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in a resolvable state");
    }

    @Test
    void alreadyResolvedAssignmentCannotBeResolvedAgain() {
        TestContext context = createContext("RESOLVED");
        ToolAssignment assignment = createActiveAssignment(context, "A");
        supervisorResolutionService.create(resolutionRequest(context, List.of(assignment.getId())));

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(context, List.of(assignment.getId()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in a resolvable state");
    }

    @Test
    void cannotResolveAssignmentFromAnotherOperator() {
        TestContext ownerContext = createContext("OWNER");
        TestContext otherOperatorContext = createContext("OTHER");
        ToolAssignment assignment = createActiveAssignment(ownerContext, "A");

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(otherOperatorContext, List.of(assignment.getId()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to operator");
    }

    @Test
    void supervisorResolutionContractDoesNotExposeAllowExit() {
        assertThat(recordComponentNames(CreateSupervisorResolutionRequest.class))
                .doesNotContain("allowExit");
        assertThat(recordComponentNames(CreateSupervisorResolutionResponse.class))
                .doesNotContain("allowExit");
    }

    @Test
    void duplicatedAssignmentIdsAreRejected() {
        TestContext context = createContext("DUP");
        ToolAssignment assignment = createActiveAssignment(context, "A");

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(context, List.of(
                assignment.getId(),
                assignment.getId()
        ))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicates");
    }

    private CreateSupervisorResolutionRequest resolutionRequest(TestContext context, List<UUID> assignmentIds) {
        return new CreateSupervisorResolutionRequest(
                context.operator.getId(),
                context.supervisor.getId(),
                "BROKEN_TOOL",
                "Tool reviewed by supervisor.",
                OffsetDateTime.now(),
                assignmentIds
        );
    }

    private TestContext createContext(String label) {
        String suffix = label + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Cabinet cabinet = cabinetRepository.save(Cabinet.newCabinet("CAB-SUP-" + suffix, "Cabinet SUP", "Lab"));
        User operator = userRepository.save(User.newUser("operator-sup-" + suffix, "Operator SUP", UserRole.OPERATOR, User.hashPin("1234"), null));
        User supervisor = userRepository.save(User.newUser("supervisor-sup-" + suffix, "Supervisor SUP", UserRole.SUPERVISOR, null, null));
        return new TestContext(cabinet, operator, supervisor);
    }

    private ToolAssignment createActiveAssignment(TestContext context, String label) {
        String suffix = label + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Tool tool = toolRepository.save(Tool.newTool(context.cabinet.getId(), "TAG-SUP-" + suffix, "Tool SUP " + label));
        return toolAssignmentRepository.save(ToolAssignment.createActive(
                tool.getId(),
                context.operator.getId(),
                context.cabinet.getId(),
                UUID.randomUUID(),
                OffsetDateTime.now()
        ));
    }

    private List<String> recordComponentNames(Class<? extends Record> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(component -> component.getName())
                .toList();
    }

    private record TestContext(Cabinet cabinet, User operator, User supervisor) {
    }
}
