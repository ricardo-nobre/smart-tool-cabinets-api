package smarttoolcabinets.supervisor.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionRequest;
import smarttoolcabinets.supervisor.dto.CreateSupervisorResolutionResponse;
import smarttoolcabinets.supervisor.dto.SupervisorResolutionListResponse;
import smarttoolcabinets.supervisor.service.SupervisorResolutionService;

import java.util.UUID;

@RestController
@RequestMapping("/api/supervisor/resolutions")
public class SupervisorResolutionController {

    private final SupervisorResolutionService supervisorResolutionService;

    public SupervisorResolutionController(SupervisorResolutionService supervisorResolutionService) {
        this.supervisorResolutionService = supervisorResolutionService;
    }

    @PostMapping
    public ResponseEntity<CreateSupervisorResolutionResponse> create(
            @Valid @RequestBody CreateSupervisorResolutionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supervisorResolutionService.create(request));
    }

    @GetMapping
    public ResponseEntity<SupervisorResolutionListResponse> list(
            @RequestParam(name = "operatorId", required = false) UUID operatorId
    ) {
        return ResponseEntity.ok(supervisorResolutionService.list(operatorId));
    }
}

