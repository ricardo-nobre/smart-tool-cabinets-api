package smarttoolcabinets.supervisor.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.operator.service.OperatorQueryService;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionReasonCode;
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
    void resolutionOfSinglePendingAssignmentAllowsExitOnNextEndOfDayCheck() {
        TestContext context = createContext("ALL");
        AssignmentContext assignmentContext = createActiveAssignment(context, "A");

        var beforeResolution = operatorQueryService.endOfDayCheck(context.operator.getId());
        assertThat(beforeResolution.pendingAssignmentsCount()).isEqualTo(1);
        assertThat(beforeResolution.requireSupervisorReview()).isTrue();
        assertThat(beforeResolution.allowExit()).isFalse();

        var response = supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Tool reviewed by supervisor."
        ));

        assertThat(response.resolvedAssignmentId()).isEqualTo(assignmentContext.assignment.getId());
        ToolAssignment resolved = toolAssignmentRepository.findById(assignmentContext.assignment.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);

        var afterResolution = operatorQueryService.endOfDayCheck(context.operator.getId());
        assertThat(afterResolution.pendingAssignmentsCount()).isZero();
        assertThat(afterResolution.requireSupervisorReview()).isFalse();
        assertThat(afterResolution.allowExit()).isTrue();

        var links = supervisorResolutionAssignmentRepository.findByIdSupervisorResolutionId(response.resolutionId());
        assertThat(links)
                .singleElement()
                .satisfies(link -> assertThat(link.getId().getToolAssignmentId()).isEqualTo(assignmentContext.assignment.getId()));
    }

    @Test
    void partialResolutionKeepsExitBlockedWhileAnotherAssignmentIsPending() {
        TestContext context = createContext("PARTIAL");
        AssignmentContext resolvedAssignment = createActiveAssignment(context, "A");
        AssignmentContext stillPendingAssignment = createActiveAssignment(context, "B");

        supervisorResolutionService.create(resolutionRequest(
                context,
                resolvedAssignment.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "First tool reviewed."
        ));

        ToolAssignment resolved = toolAssignmentRepository.findById(resolvedAssignment.assignment.getId()).orElseThrow();
        ToolAssignment stillPending = toolAssignmentRepository.findById(stillPendingAssignment.assignment.getId()).orElseThrow();

        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);
        assertThat(stillPending.getStatus()).isEqualTo(ToolAssignmentStatus.ACTIVE);

        var endOfDay = operatorQueryService.endOfDayCheck(context.operator.getId());
        assertThat(endOfDay.pendingAssignmentsCount()).isEqualTo(1);
        assertThat(endOfDay.requireSupervisorReview()).isTrue();
        assertThat(endOfDay.allowExit()).isFalse();
        assertThat(endOfDay.pendingAssignments())
                .singleElement()
                .satisfies(item -> assertThat(item.assignmentId()).isEqualTo(stillPendingAssignment.assignment.getId()));
    }

    @Test
    void pendingReviewAssignmentCanBeResolved() {
        TestContext context = createContext("PENDING");
        AssignmentContext assignmentContext = createActiveAssignment(context, "A");
        assignmentContext.assignment.markPendingReview(context.cabinet.getId(), UUID.randomUUID(), OffsetDateTime.now());
        toolAssignmentRepository.save(assignmentContext.assignment);

        supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Pending review closed."
        ));

        ToolAssignment resolved = toolAssignmentRepository.findById(assignmentContext.assignment.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo(ToolAssignmentStatus.RESOLVED);
    }

    @Test
    void secondResolutionForSameAssignmentIsRejected() {
        TestContext context = createContext("SECOND");
        AssignmentContext assignmentContext = createActiveAssignment(context, "A");

        supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "First review."
        ));

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                SupervisorResolutionReasonCode.OTHER,
                "Second review."
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has supervisor resolution");
    }

    @Test
    void returnedAndResolvedAssignmentsCannotBeResolved() {
        TestContext returnedContext = createContext("RETURNED");
        AssignmentContext returnedAssignment = createActiveAssignment(returnedContext, "A");
        returnedAssignment.assignment.markReturned(returnedContext.cabinet.getId(), UUID.randomUUID(), OffsetDateTime.now());
        toolAssignmentRepository.save(returnedAssignment.assignment);

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                returnedContext,
                returnedAssignment.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Returned tool."
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in a resolvable state");

        TestContext resolvedContext = createContext("RESOLVED");
        AssignmentContext resolvedAssignment = createActiveAssignment(resolvedContext, "A");
        resolvedAssignment.assignment.markResolved();
        toolAssignmentRepository.save(resolvedAssignment.assignment);

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                resolvedContext,
                resolvedAssignment.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Resolved tool."
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not in a resolvable state");
    }

    @Test
    void cannotResolveAssignmentFromAnotherOperator() {
        TestContext ownerContext = createContext("OWNER");
        TestContext otherOperatorContext = createContext("OTHER");
        AssignmentContext assignmentContext = createActiveAssignment(ownerContext, "A");

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                otherOperatorContext,
                assignmentContext.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Wrong operator."
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong to operator");
    }

    @Test
    void invalidSupervisorIsRejected() {
        TestContext context = createContext("SUP");
        AssignmentContext assignmentContext = createActiveAssignment(context, "A");
        User inactiveSupervisor = userRepository.save(User.newUser(
                "inactive-supervisor-" + UUID.randomUUID(),
                "Inactive Supervisor",
                UserRole.SUPERVISOR,
                null,
                null
        ));
        inactiveSupervisor.deactivate();
        userRepository.save(inactiveSupervisor);
        User notSupervisor = userRepository.save(User.newUser(
                "not-supervisor-" + UUID.randomUUID(),
                "Not Supervisor",
                UserRole.OPERATOR,
                User.hashPin("1234"),
                null
        ));

        assertThatThrownBy(() -> supervisorResolutionService.create(new CreateSupervisorResolutionRequest(
                context.operator.getId(),
                UUID.randomUUID(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Missing supervisor.",
                OffsetDateTime.now(),
                assignmentContext.assignment.getId()
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supervisor not found");

        assertThatThrownBy(() -> supervisorResolutionService.create(new CreateSupervisorResolutionRequest(
                context.operator.getId(),
                inactiveSupervisor.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Inactive supervisor.",
                OffsetDateTime.now(),
                assignmentContext.assignment.getId()
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supervisor must be active");

        assertThatThrownBy(() -> supervisorResolutionService.create(new CreateSupervisorResolutionRequest(
                context.operator.getId(),
                notSupervisor.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "Wrong role.",
                OffsetDateTime.now(),
                assignmentContext.assignment.getId()
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role SUPERVISOR");
    }

    @Test
    void reportAndReasonCodeAreRequiredAndValidated() {
        TestContext context = createContext("VALIDATION");
        AssignmentContext assignmentContext = createActiveAssignment(context, "A");

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                null,
                "Valid report."
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reasonCode is invalid");

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                "INVALID",
                "Valid report."
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reasonCode is invalid");

        assertThatThrownBy(() -> supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                SupervisorResolutionReasonCode.MANUAL_VERIFICATION,
                "   "
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reportText is required");
    }

    @Test
    void reasonCodesCanDeactivateToolWhenOccurrenceRequiresIt() {
        assertToolActiveAfterResolution(SupervisorResolutionReasonCode.TOOL_LOST, false);
        assertToolActiveAfterResolution(SupervisorResolutionReasonCode.TOOL_DAMAGED, false);
        assertToolActiveAfterResolution(SupervisorResolutionReasonCode.RFID_FAILURE, false);
        assertToolActiveAfterResolution(SupervisorResolutionReasonCode.MANUAL_VERIFICATION, true);
    }

    @Test
    void supervisorResolutionContractUsesSingularAssignmentAndDoesNotExposeAllowExit() {
        assertThat(recordComponentNames(CreateSupervisorResolutionRequest.class))
                .contains("assignmentId")
                .doesNotContain("assignmentIds", "allowExit");
        assertThat(recordComponentNames(CreateSupervisorResolutionResponse.class))
                .contains("resolvedAssignmentId")
                .doesNotContain("resolvedAssignmentIds", "allowExit");
    }

    private void assertToolActiveAfterResolution(String reasonCode, boolean expectedActive) {
        TestContext context = createContext(reasonCode);
        AssignmentContext assignmentContext = createActiveAssignment(context, reasonCode);

        supervisorResolutionService.create(resolutionRequest(
                context,
                assignmentContext.assignment.getId(),
                reasonCode,
                "Reason code test."
        ));

        Tool tool = toolRepository.findById(assignmentContext.tool.getId()).orElseThrow();
        assertThat(tool.isActive()).isEqualTo(expectedActive);
    }

    private CreateSupervisorResolutionRequest resolutionRequest(
            TestContext context,
            UUID assignmentId,
            String reasonCode,
            String reportText
    ) {
        return new CreateSupervisorResolutionRequest(
                context.operator.getId(),
                context.supervisor.getId(),
                reasonCode,
                reportText,
                OffsetDateTime.now(),
                assignmentId
        );
    }

    private TestContext createContext(String label) {
        String normalizedLabel = label.length() > 12 ? label.substring(0, 12) : label;
        String suffix = normalizedLabel + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase();
        Cabinet cabinet = cabinetRepository.save(Cabinet.newCabinet("CAB-SUP-" + suffix, "Cabinet SUP", "Lab"));
        User operator = userRepository.save(User.newUser("operator-sup-" + suffix, "Operator SUP", UserRole.OPERATOR, User.hashPin("1234"), null));
        User supervisor = userRepository.save(User.newUser("supervisor-sup-" + suffix, "Supervisor SUP", UserRole.SUPERVISOR, null, null));
        return new TestContext(cabinet, operator, supervisor);
    }

    private AssignmentContext createActiveAssignment(TestContext context, String label) {
        String suffix = label + "-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Tool tool = toolRepository.save(Tool.newTool(context.cabinet.getId(), "TAG-SUP-" + suffix, "Tool SUP " + label));
        ToolAssignment assignment = toolAssignmentRepository.save(ToolAssignment.createActive(
                tool.getId(),
                context.operator.getId(),
                context.cabinet.getId(),
                UUID.randomUUID(),
                OffsetDateTime.now()
        ));
        return new AssignmentContext(tool, assignment);
    }

    private java.util.List<String> recordComponentNames(Class<? extends Record> recordType) {
        return Arrays.stream(recordType.getRecordComponents())
                .map(component -> component.getName())
                .toList();
    }

    private record TestContext(Cabinet cabinet, User operator, User supervisor) {
    }

    private record AssignmentContext(Tool tool, ToolAssignment assignment) {
    }
}
