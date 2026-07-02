package smarttoolcabinets.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.domain.UserRole;
import smarttoolcabinets.user.dto.AdminUserCreateRequest;
import smarttoolcabinets.user.repository.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public String createUser(AdminUserCreateRequest request) {
        String username = request.username().trim();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        String role = request.role().trim().toUpperCase();
        if (!UserRole.SUPPORTED.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        String pinHash = null;
        if (request.pin() != null && !request.pin().isBlank()) {
            pinHash = User.hashPin(request.pin().trim());
        }

        String nfcUid = null;
        if (request.nfcUid() != null && !request.nfcUid().isBlank()) {
            nfcUid = request.nfcUid().trim().toUpperCase();
            if (userRepository.findByNfcUid(nfcUid).isPresent()) {
                throw new IllegalArgumentException("NFC UID already exists: " + nfcUid);
            }
        }

        if (UserRole.OPERATOR.equals(role) && pinHash == null && nfcUid == null) {
            throw new IllegalArgumentException("Operator must provide at least pin or nfcUid");
        }

        User user = User.newUser(username, request.fullName(), role, pinHash, nfcUid);
        User u = userRepository.save(user);
        return u.getId().toString();
    }
}

