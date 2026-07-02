package smarttoolcabinets.cabinetaccess.service;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.cabinetaccess.dto.OperatorAuthRequest;
import smarttoolcabinets.cabinetaccess.dto.OperatorAuthResponse;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.domain.UserRole;
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

        String cabinetCode = requiredText(request.cabinetCode(), "cabinetCode is required");

        var cabinet = cabinetRepository.findByCode(cabinetCode)
                .orElseThrow(() -> new IllegalArgumentException("cabinet not found for code: " + cabinetCode));

        if (!cabinet.isActive()) {
            throw new IllegalStateException("cabinet is not active: " + cabinetCode);
        }

        String credential = requiredText(request.credential(), "credential is required");
        if (request.method() == null) {
            throw new IllegalArgumentException("method is required");
        }

        Optional<User> user = switch (request.method()) {
            case PIN -> userRepository.findByPinHash(User.hashPin(credential));
            case NFC -> userRepository.findByNfcUid(credential.toUpperCase(Locale.ROOT));
        };

        User operator = user.orElseThrow(() -> new BadCredentialsException("Invalid operator credential"));
        if (!operator.isActive()) {
            throw new IllegalStateException("operator is not active: " + operator.getId());
        }
        if (!UserRole.OPERATOR.equalsIgnoreCase(operator.getRole())) {
            throw new BadCredentialsException("Invalid operator credential");
        }

        return new OperatorAuthResponse(operator.getId(), "AUTHENTICATED");
    }

    private String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}

