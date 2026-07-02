package smarttoolcabinets.cabinet.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.cabinet.dto.DeviceAuthRequest;
import smarttoolcabinets.cabinet.dto.DeviceAuthResponse;
import smarttoolcabinets.cabinet.service.DeviceAuthService;

@RestController
@RequestMapping("/api/device/auth")
public class DeviceAuthController {

    private final DeviceAuthService deviceAuthService;

    public DeviceAuthController(DeviceAuthService deviceAuthService) {
        this.deviceAuthService = deviceAuthService;
    }

    @PostMapping
    public ResponseEntity<DeviceAuthResponse> authenticate(@Valid @RequestBody DeviceAuthRequest request) {
        return ResponseEntity.ok(deviceAuthService.authenticate(request));
    }
}

