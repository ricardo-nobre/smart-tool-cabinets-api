package smarttoolcabinets.operator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.operator.dto.EndOfDayCheckResponse;
import smarttoolcabinets.operator.dto.OperatorToolAssignmentsResponse;
import smarttoolcabinets.operator.service.OperatorQueryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/operators")
public class OperatorController {

    private final OperatorQueryService operatorQueryService;

    public OperatorController(OperatorQueryService operatorQueryService) {
        this.operatorQueryService = operatorQueryService;
    }

    @GetMapping("/{operatorId}/tool-assignments")
    public ResponseEntity<OperatorToolAssignmentsResponse> getToolAssignments(
            @PathVariable UUID operatorId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(operatorQueryService.getAssignments(operatorId, status));
    }

    @GetMapping("/{operatorId}/end-of-day-check")
    public ResponseEntity<EndOfDayCheckResponse> endOfDayCheck(@PathVariable UUID operatorId) {
        return ResponseEntity.ok(operatorQueryService.endOfDayCheck(operatorId));
    }
}

