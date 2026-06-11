package smarttoolcabinets.session.service;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.session.dto.OperatorAuthRequest;
import smarttoolcabinets.session.dto.OperatorAuthResponse;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.repository.UserRepository;

import java.util.Locale;
import java.util.Optional;

/**
 * Service para autenticacao de operador (PIN/NFC) no armario.
 */
@Service
public class DeviceOperatorAuthService {

    private final CabinetRepository cabinetRepository;
    private final UserRepository userRepository;

    public DeviceOperatorAuthService(CabinetRepository cabinetRepository, UserRepository userRepository) {
        this.cabinetRepository = cabinetRepository;
        this.userRepository = userRepository;
    }

    public OperatorAuthResponse authenticate(OperatorAuthRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        String cabinetCode = request.cabinetCode() == null ? "" : request.cabinetCode().trim();
        if (cabinetCode.isBlank()) {
            throw new IllegalArgumentException("cabinetCode is required");
        }

        var cabinet = cabinetRepository.findByCode(cabinetCode)
                .orElseThrow(() -> new IllegalArgumentException("cabinet not found for code: " + cabinetCode));

        if (!cabinet.isActive()) {
            throw new IllegalStateException("cabinet is not active: " + cabinetCode);
        }

        String credential = request.credential() == null ? "" : request.credential().trim();
        if (credential.isBlank()) {
            throw new IllegalArgumentException("credential is required");
        }

        Optional<User> user = switch (request.method()) {
            case PIN -> userRepository.findByPinHash(credential);
            case NFC -> userRepository.findByNfcUid(credential.toUpperCase(Locale.ROOT));
        };

        User operator = user.orElseThrow(() -> new BadCredentialsException("Invalid operator credential"));
        if (!operator.isActive()) {
            throw new IllegalStateException("operator is not active: " + operator.getId());
        }
        if (!"OPERATOR".equalsIgnoreCase(operator.getRole())) {
            throw new BadCredentialsException("Invalid operator credential");
        }

        return new OperatorAuthResponse(operator.getId(), "AUTHENTICATED");
    }
}

