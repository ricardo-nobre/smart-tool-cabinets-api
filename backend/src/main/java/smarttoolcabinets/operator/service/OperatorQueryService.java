package smarttoolcabinets.operator.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.operator.dto.EndOfDayCheckResponse;
import smarttoolcabinets.operator.dto.OperatorToolAssignmentsResponse;
import smarttoolcabinets.operator.dto.ToolAssignmentItem;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.domain.ToolAssignmentStatus;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Consultas de custodia por operador.
 */
@Service
public class OperatorQueryService {

    private final ToolAssignmentRepository toolAssignmentRepository;
    private final ToolRepository toolRepository;
    private final CabinetRepository cabinetRepository;
    private final UserRepository userRepository;

    public OperatorQueryService(
            ToolAssignmentRepository toolAssignmentRepository,
            ToolRepository toolRepository,
            CabinetRepository cabinetRepository,
            UserRepository userRepository
    ) {
        this.toolAssignmentRepository = toolAssignmentRepository;
        this.toolRepository = toolRepository;
        this.cabinetRepository = cabinetRepository;
        this.userRepository = userRepository;
    }

    public OperatorToolAssignmentsResponse getAssignments(UUID operatorId, String status) {
        List<ToolAssignment> assignments;
        if (status == null || status.isBlank()) {
            assignments = toolAssignmentRepository.findByOperatorId(operatorId);
        } else {
            assignments = toolAssignmentRepository.findByOperatorIdAndStatus(operatorId, status.trim().toUpperCase(Locale.ROOT));
        }

        return new OperatorToolAssignmentsResponse(
                operatorId,
                assignments.stream().map(this::toItem).toList()
        );
    }

    public EndOfDayCheckResponse endOfDayCheck(UUID operatorId) {
        var operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("operator not found: " + operatorId));
        if (!UserRole.OPERATOR.equalsIgnoreCase(operator.getRole())) {
            throw new IllegalArgumentException("operatorId must reference role OPERATOR");
        }
        if (!operator.isActive()) {
            throw new IllegalArgumentException("operator must be active");
        }

        List<ToolAssignment> pendingAssignments = toolAssignmentRepository.findByOperatorId(operatorId).stream()
                .filter(assignment -> ToolAssignmentStatus.ACTIVE.equalsIgnoreCase(assignment.getStatus())
                        || ToolAssignmentStatus.PENDING_REVIEW.equalsIgnoreCase(assignment.getStatus()))
                .toList();

        List<ToolAssignmentItem> items = pendingAssignments.stream().map(this::toItem).toList();
        int pendingAssignmentsCount = items.size();
        boolean requireSupervisorReview = pendingAssignmentsCount > 0;
        boolean allowExit = pendingAssignmentsCount == 0;

        return new EndOfDayCheckResponse(
                operatorId,
                items,
                pendingAssignmentsCount,
                requireSupervisorReview,
                allowExit
        );
    }

    private ToolAssignmentItem toItem(ToolAssignment assignment) {
        var tool = toolRepository.findById(assignment.getToolId()).orElse(null);
        var cabinet = cabinetRepository.findById(assignment.getOriginCabinetId()).orElse(null);

        return new ToolAssignmentItem(
                assignment.getId(),
                assignment.getToolId(),
                tool == null ? null : tool.getTagCode(),
                tool == null ? null : tool.getDisplayName(),
                cabinet == null ? null : cabinet.getCode(),
                assignment.getAssignedAt(),
                assignment.getReturnedAt(),
                assignment.getStatus()
        );
    }
}
