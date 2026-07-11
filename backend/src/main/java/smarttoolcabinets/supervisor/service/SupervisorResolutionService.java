package smarttoolcabinets.supervisor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.supervisor.domain.SupervisorResolution;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionAssignment;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionAssignmentId;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionReasonCode;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionRequest;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionResponse;
import smarttoolcabinets.supervisor.dto.SupervisorResolutionListResponse;
import smarttoolcabinets.supervisor.repository.SupervisorResolutionAssignmentRepository;
import smarttoolcabinets.supervisor.repository.SupervisorResolutionRepository;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.domain.ToolAssignmentStatus;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class SupervisorResolutionService {

    private final SupervisorResolutionRepository supervisorResolutionRepository;
    private final SupervisorResolutionAssignmentRepository supervisorResolutionAssignmentRepository;
    private final ToolAssignmentRepository toolAssignmentRepository;
    private final ToolRepository toolRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public SupervisorResolutionService(
            SupervisorResolutionRepository supervisorResolutionRepository,
            SupervisorResolutionAssignmentRepository supervisorResolutionAssignmentRepository,
            ToolAssignmentRepository toolAssignmentRepository,
            ToolRepository toolRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.supervisorResolutionRepository = supervisorResolutionRepository;
        this.supervisorResolutionAssignmentRepository = supervisorResolutionAssignmentRepository;
        this.toolAssignmentRepository = toolAssignmentRepository;
        this.toolRepository = toolRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CreateSupervisorResolutionResponse create(CreateSupervisorResolutionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.operatorId() == null) {
            throw new IllegalArgumentException("operatorId is required");
        }
        if (request.supervisorId() == null) {
            throw new IllegalArgumentException("supervisorId is required");
        }
        if (request.assignmentId() == null) {
            throw new IllegalArgumentException("assignmentId is required");
        }
        if (request.decisionAt() == null) {
            throw new IllegalArgumentException("decisionAt is required");
        }

        var operator = userRepository.findById(request.operatorId())
                .orElseThrow(() -> new IllegalArgumentException("operator not found: " + request.operatorId()));
        if (!UserRole.OPERATOR.equalsIgnoreCase(operator.getRole())) {
            throw new IllegalArgumentException("operatorId must reference role OPERATOR");
        }
        if (!operator.isActive()) {
            throw new IllegalArgumentException("operator must be active");
        }

        var supervisor = userRepository.findById(request.supervisorId())
                .orElseThrow(() -> new IllegalArgumentException("supervisor not found: " + request.supervisorId()));
        if (!UserRole.SUPERVISOR.equalsIgnoreCase(supervisor.getRole())) {
            throw new IllegalArgumentException("supervisorId must reference role SUPERVISOR");
        }
        if (!supervisor.isActive()) {
            throw new IllegalArgumentException("supervisor must be active");
        }

        String reasonCode = SupervisorResolutionReasonCode.normalize(request.reasonCode());
        if (!SupervisorResolutionReasonCode.SUPPORTED.contains(reasonCode)) {
            throw new IllegalArgumentException("reasonCode is invalid: " + request.reasonCode());
        }
        if (request.reportText() == null || request.reportText().isBlank()) {
            throw new IllegalArgumentException("reportText is required");
        }
        String reportText = request.reportText().trim();

        ToolAssignment assignment = toolAssignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new IllegalArgumentException("assignment not found: " + request.assignmentId()));
        if (!assignment.getOperatorId().equals(request.operatorId())) {
            throw new IllegalArgumentException("assignment does not belong to operator: " + assignment.getId());
        }
        if (supervisorResolutionAssignmentRepository.existsByIdToolAssignmentId(assignment.getId())) {
            throw new IllegalArgumentException("assignment already has supervisor resolution: " + assignment.getId());
        }
        if (!isResolvable(assignment)) {
            throw new IllegalArgumentException("assignment is not in a resolvable state: " + assignment.getId());
        }

        SupervisorResolution resolution = SupervisorResolution.create(
                request.operatorId(),
                request.supervisorId(),
                reasonCode,
                reportText,
                request.decisionAt()
        );

        SupervisorResolution saved = supervisorResolutionRepository.save(resolution);

        assignment.markResolved();
        toolAssignmentRepository.save(assignment);

        if (SupervisorResolutionReasonCode.deactivatesTool(reasonCode)) {
            var tool = toolRepository.findById(assignment.getToolId())
                    .orElseThrow(() -> new IllegalStateException("tool not found for assignment: " + assignment.getToolId()));
            tool.deactivate();
            toolRepository.save(tool);
        }

        supervisorResolutionAssignmentRepository.save(SupervisorResolutionAssignment.create(
                new SupervisorResolutionAssignmentId(saved.getId(), assignment.getId())
        ));

        auditService.logAction(
                UserRole.SUPERVISOR,
                request.supervisorId().toString(),
                "CREATE_SUPERVISOR_RESOLUTION",
                AuditEntityType.SUPERVISOR_RESOLUTION,
                saved.getId()
        );

        return toResponse(saved, assignment.getId());
    }

    public SupervisorResolutionListResponse list(UUID operatorId) {
        List<SupervisorResolution> items = operatorId == null
                ? supervisorResolutionRepository.findAll()
                : supervisorResolutionRepository.findByOperatorId(operatorId);

        List<CreateSupervisorResolutionResponse> responseItems = items.stream()
                .map(item -> {
                    UUID assignmentId = supervisorResolutionAssignmentRepository
                            .findByIdSupervisorResolutionId(item.getId())
                            .stream()
                            .findFirst()
                            .map(link -> link.getId().getToolAssignmentId())
                            .orElse(null);
                    return toResponse(item, assignmentId);
                })
                .toList();

        return new SupervisorResolutionListResponse(responseItems);
    }

    private CreateSupervisorResolutionResponse toResponse(SupervisorResolution resolution, UUID assignmentId) {
        return new CreateSupervisorResolutionResponse(
                resolution.getId(),
                resolution.getOperatorId(),
                resolution.getSupervisorId(),
                resolution.getDecisionAt(),
                resolution.getReasonCode(),
                resolution.getReportText(),
                assignmentId
        );
    }

    private boolean isResolvable(ToolAssignment assignment) {
        return ToolAssignmentStatus.ACTIVE.equalsIgnoreCase(assignment.getStatus())
                || ToolAssignmentStatus.PENDING_REVIEW.equalsIgnoreCase(assignment.getStatus());
    }
}
