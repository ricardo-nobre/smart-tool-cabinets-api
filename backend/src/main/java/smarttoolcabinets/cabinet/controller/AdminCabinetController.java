package smarttoolcabinets.cabinet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.cabinet.dto.AdminCabinetCreateRequest;
import smarttoolcabinets.cabinet.service.AdminCabinetService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/cabinets")
public class AdminCabinetController {

    private final AdminCabinetService adminCabinetService;

    public AdminCabinetController(AdminCabinetService adminCabinetService) {
        this.adminCabinetService = adminCabinetService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createCabinet(@Valid @RequestBody AdminCabinetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("cabinetId", adminCabinetService.createCabinet(request)));
    }
}

