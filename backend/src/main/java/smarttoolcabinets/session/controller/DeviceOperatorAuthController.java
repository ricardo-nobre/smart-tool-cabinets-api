package smarttoolcabinets.session.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.session.dto.OperatorAuthRequest;
import smarttoolcabinets.session.dto.OperatorAuthResponse;
import smarttoolcabinets.session.service.DeviceOperatorAuthService;

/**
 * Endpoint de autenticacao de operador no armario.
 */
@RestController
@RequestMapping("/api/device/operator-auth")
public class DeviceOperatorAuthController {

    private final DeviceOperatorAuthService deviceOperatorAuthService;

    public DeviceOperatorAuthController(DeviceOperatorAuthService deviceOperatorAuthService) {
        this.deviceOperatorAuthService = deviceOperatorAuthService;
    }

    @PostMapping
    public ResponseEntity<OperatorAuthResponse> authenticate(@Valid @RequestBody OperatorAuthRequest request) {
        return ResponseEntity.ok(deviceOperatorAuthService.authenticate(request));
    }
}

