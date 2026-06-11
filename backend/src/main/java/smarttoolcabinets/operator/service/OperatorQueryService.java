package smarttoolcabinets.operator.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.operator.dto.EndOfDayCheckResponse;
import smarttoolcabinets.operator.dto.OperatorToolAssignmentsResponse;
import smarttoolcabinets.operator.dto.ToolAssignmentItem;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.toolassignment.domain.ToolAssignment;
import smarttoolcabinets.toolassignment.repository.ToolAssignmentRepository;

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

    public OperatorQueryService(
            ToolAssignmentRepository toolAssignmentRepository,
            ToolRepository toolRepository,
            CabinetRepository cabinetRepository
    ) {
        this.toolAssignmentRepository = toolAssignmentRepository;
        this.toolRepository = toolRepository;
        this.cabinetRepository = cabinetRepository;
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
        List<ToolAssignment> pendingAssignments = toolAssignmentRepository.findByOperatorId(operatorId).stream()
                .filter(assignment -> "ACTIVE".equalsIgnoreCase(assignment.getStatus())
                        || "PENDING_REVIEW".equalsIgnoreCase(assignment.getStatus()))
                .toList();

        List<ToolAssignmentItem> items = pendingAssignments.stream().map(this::toItem).toList();
        boolean requireSupervisorReview = !items.isEmpty();

        return new EndOfDayCheckResponse(
                operatorId,
                items,
                items.size(),
                requireSupervisorReview,
                !requireSupervisorReview
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

