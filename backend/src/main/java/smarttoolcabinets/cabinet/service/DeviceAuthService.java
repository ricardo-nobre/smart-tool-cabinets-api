package smarttoolcabinets.cabinet.service;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.dto.DeviceAuthRequest;
import smarttoolcabinets.cabinet.dto.DeviceAuthResponse;
import smarttoolcabinets.cabinet.repository.CabinetRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Service de autenticacao de dispositivos de armario.
 *
 * Centraliza regras de autenticacao de dispositivo por credencial tecnica.
 */
@Service
public class DeviceAuthService {

    private final CabinetRepository cabinetRepository;

    public DeviceAuthService(CabinetRepository cabinetRepository) {
        this.cabinetRepository = cabinetRepository;
    }

    /**
     * Objetivo: autenticar um armario via cabinetCode + apiKey.
     */
    public DeviceAuthResponse authenticate(DeviceAuthRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if(request.apiKey() == null || request.cabinetCode() == null) {
            throw new IllegalArgumentException("Credenciais incompletas");
        }
        if(request.apiKey().isBlank() || request.cabinetCode().isBlank()) {
            throw new IllegalArgumentException("Credenciais incompletas");
        }
        if (request.apiKey().length() > 255 || request.cabinetCode().length() > 64) {
            throw new IllegalArgumentException("Credenciais invalidas");
        }
        String cabinetCode = request.cabinetCode().trim();
        Optional<Cabinet> cabinetOpt = cabinetRepository.findByCode(cabinetCode);
        if (cabinetOpt.isEmpty()) {
            throw new BadCredentialsException("Credenciais invalidas");
        }
        Cabinet cabinet = cabinetOpt.get();
        if(!cabinet.isActive()) {
            throw new IllegalStateException("Cabinet inativo");
        }
        String requestApiKeyHash = Cabinet.hashApiKey(request.apiKey().trim());
        if (!requestApiKeyHash.equals(cabinet.getApiKeyHash())) {
            throw new BadCredentialsException("Credenciais invalidas");
        }
        String tempToken = "DEV-TOKEN-" + cabinet.getCode();
        return new DeviceAuthResponse(tempToken, OffsetDateTime.now().plusHours(8));
    }
}

