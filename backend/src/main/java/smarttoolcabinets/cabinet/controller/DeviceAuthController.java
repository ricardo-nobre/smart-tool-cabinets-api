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

/**
 * Controller de autenticacao do dispositivo (armario).
 *
 * Expõe o contrato HTTP inicial para autenticar armarios por API key.
 * Evolucao futura: seguranca final e estrategia de token de dispositivo.
 */
@RestController
@RequestMapping("/api/device/auth")
public class DeviceAuthController {

    private final DeviceAuthService deviceAuthService;

    public DeviceAuthController(DeviceAuthService deviceAuthService) {
        this.deviceAuthService = deviceAuthService;
    }

    /**
     * Objetivo: autenticar armario por API key.
     * Inputs esperados: cabinetCode e apiKey no body JSON.
     * Output esperado: token de dispositivo e mensagem de estado.
     * Passos logicos a implementar:
     * 1) Validar payload.
     * 2) Delegar autenticacao ao service.
     * 3) Mapear erros de auth para HTTP apropriado.
     * Notas: endpoint sera protegido por politica especifica em SecurityConfig.
     */
    @PostMapping
    public ResponseEntity<DeviceAuthResponse> authenticate(@Valid @RequestBody DeviceAuthRequest request) {
        return ResponseEntity.ok(deviceAuthService.authenticate(request));
    }
}

