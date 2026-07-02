package smarttoolcabinets.cabinet.service;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.dto.DeviceAuthRequest;
import smarttoolcabinets.cabinet.dto.DeviceAuthResponse;
import smarttoolcabinets.cabinet.repository.CabinetRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class DeviceAuthService {

    private final CabinetRepository cabinetRepository;

    public DeviceAuthService(CabinetRepository cabinetRepository) {
        this.cabinetRepository = cabinetRepository;
    }

    public DeviceAuthResponse authenticate(DeviceAuthRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        String cabinetCode = requiredText(request.cabinetCode(), "Credenciais incompletas");
        String apiKey = requiredText(request.apiKey(), "Credenciais incompletas");
        if (apiKey.length() > 255 || cabinetCode.length() > 64) {
            throw new IllegalArgumentException("Credenciais invalidas");
        }
        Optional<Cabinet> cabinetOpt = cabinetRepository.findByCode(cabinetCode);
        if (cabinetOpt.isEmpty()) {
            throw new BadCredentialsException("Credenciais invalidas");
        }
        Cabinet cabinet = cabinetOpt.get();
        if(!cabinet.isActive()) {
            throw new IllegalStateException("Cabinet inativo");
        }
        String requestApiKeyHash = Cabinet.hashApiKey(apiKey);
        if (!requestApiKeyHash.equals(cabinet.getApiKeyHash())) {
            throw new BadCredentialsException("Credenciais invalidas");
        }
        String tempToken = "DEV-TOKEN-" + cabinet.getCode();
        return new DeviceAuthResponse(tempToken, OffsetDateTime.now().plusHours(8));
    }

    private String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}

