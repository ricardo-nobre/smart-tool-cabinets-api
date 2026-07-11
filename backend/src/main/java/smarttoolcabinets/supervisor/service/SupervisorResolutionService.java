package smarttoolcabinets.supervisor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.supervisor.domain.SupervisorResolution;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionAssignment;
import smarttoolcabinets.supervisor.domain.SupervisorResolutionAssignmentId;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionRequest;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionResponse;
import smarttoolcabinets.supervisor.dto.SupervisorResolutionListResponse;
import smarttoolcabinets.supervisor.repository.SupervisorResolutionAssignmentRepository;
import smarttoolcabinets.supervisor.repository.SupervisorResolutionRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.domain.ToolAssignmentStatus;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SupervisorResolutionService {

    private final SupervisorResolutionRepository supervisorResolutionRepository;
    private final SupervisorResolutionAssignmentRepository supervisorResolutionAssignmentRepository;
    private final ToolAssignmentRepository toolAssignmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public SupervisorResolutionService(
            SupervisorResolutionRepository supervisorResolutionRepository,
            SupervisorResolutionAssignmentRepository supervisorResolutionAssignmentRepository,
            ToolAssignmentRepository toolAssignmentRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.supervisorResolutionRepository = supervisorResolutionRepository;
        this.supervisorResolutionAssignmentRepository = supervisorResolutionAssignmentRepository;
        this.toolAssignmentRepository = toolAssignmentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CreateSupervisorResolutionResponse create(CreateSupervisorResolutionRequest request) {
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

        List<UUID> assignmentIds = request.assignmentIds();
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            throw new IllegalArgumentException("assignmentIds must not be empty");
        }
        Set<UUID> uniqueAssignmentIds = new HashSet<>(assignmentIds);
        if (uniqueAssignmentIds.size() != assignmentIds.size()) {
            throw new IllegalArgumentException("assignmentIds must not contain duplicates");
        }

        List<ToolAssignment> assignments = toolAssignmentRepository.findAllById(assignmentIds);
        if (assignments.size() != assignmentIds.size()) {
            throw new IllegalArgumentException("One or more assignmentIds were not found");
        }

        for (ToolAssignment assignment : assignments) {
            if (!assignment.getOperatorId().equals(request.operatorId())) {
                throw new IllegalArgumentException("assignment does not belong to operator: " + assignment.getId());
            }
            if (!isResolvable(assignment)) {
                throw new IllegalArgumentException("assignment is not in a resolvable state: " + assignment.getId());
            }
        }

        SupervisorResolution resolution = SupervisorResolution.create(
                request.operatorId(),
                request.supervisorId(),
                request.reasonCode().trim(),
                request.reportText().trim(),
                request.decisionAt()
        );

        SupervisorResolution saved = supervisorResolutionRepository.save(resolution);

        List<SupervisorResolutionAssignment> links = new ArrayList<>();
        for (ToolAssignment assignment : assignments) {
            assignment.markResolved();
            links.add(SupervisorResolutionAssignment.create(
                    new SupervisorResolutionAssignmentId(saved.getId(), assignment.getId())
            ));
        }

        toolAssignmentRepository.saveAll(assignments);
        supervisorResolutionAssignmentRepository.saveAll(links);

        auditService.logAction(
                UserRole.SUPERVISOR,
                request.supervisorId().toString(),
                "CREATE_SUPERVISOR_RESOLUTION",
                AuditEntityType.SUPERVISOR_RESOLUTION,
                saved.getId()
        );

        return toResponse(saved, assignmentIds);
    }

    public SupervisorResolutionListResponse list(UUID operatorId) {
        List<SupervisorResolution> items = operatorId == null
                ? supervisorResolutionRepository.findAll()
                : supervisorResolutionRepository.findByOperatorId(operatorId);

        List<CreateSupervisorResolutionResponse> responseItems = items.stream()
                .map(item -> {
                    List<UUID> assignmentIds = supervisorResolutionAssignmentRepository
                            .findByIdSupervisorResolutionId(item.getId())
                            .stream()
                            .map(link -> link.getId().getToolAssignmentId())
                            .toList();
                    return toResponse(item, assignmentIds);
                })
                .toList();

        return new SupervisorResolutionListResponse(responseItems);
    }

    private CreateSupervisorResolutionResponse toResponse(SupervisorResolution resolution, List<UUID> assignmentIds) {
        return new CreateSupervisorResolutionResponse(
                resolution.getId(),
                resolution.getOperatorId(),
                resolution.getSupervisorId(),
                resolution.getDecisionAt(),
                resolution.getReasonCode(),
                resolution.getReportText(),
                assignmentIds
        );
    }

    private boolean isResolvable(ToolAssignment assignment) {
        return ToolAssignmentStatus.ACTIVE.equalsIgnoreCase(assignment.getStatus())
                || ToolAssignmentStatus.PENDING_REVIEW.equalsIgnoreCase(assignment.getStatus());
    }
}
