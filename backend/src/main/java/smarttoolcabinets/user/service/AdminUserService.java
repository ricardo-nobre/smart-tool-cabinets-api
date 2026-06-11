package smarttoolcabinets.user.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.user.domain.User;
import smarttoolcabinets.user.dto.AdminUserCreateRequest;
import smarttoolcabinets.user.repository.UserRepository;

/**
 * Service administrativo para gestao de utilizadores.
 *
 * Evolucao futura: validacao de roles e persistencia segura de credenciais.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Objetivo: criar utilizador administrativo/operacional.
     * Inputs esperados: username, nome e role.
     * Output esperado: id do utilizador criado.
     * Passos logicos a implementar:
     * 1) Validar formato e unicidade de username.
     * 2) Validar role permitida.
     * 3) Persistir entidade User.
     * 4) Registar auditoria de criacao.
     * Notas: alinhar com estrategia final de seguranca.
     */
    public String createUser(AdminUserCreateRequest request) {
        String username = request.username().trim();
        if(userRepository.findByUsername(username).isPresent()){
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        String role = request.role().trim().toUpperCase();
        if(!role.equals("ADMIN") && !role.equals("OPERATOR") && !role.equals("SUPERVISOR")){
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        String pinHash = null;
        if (request.pin() != null && !request.pin().isBlank()) {
            // TODO(phase-next): substituir por hash seguro (bcrypt/argon2).
            pinHash = request.pin().trim();
        }

        String nfcUid = null;
        if (request.nfcUid() != null && !request.nfcUid().isBlank()) {
            nfcUid = request.nfcUid().trim().toUpperCase();
            if (userRepository.findByNfcUid(nfcUid).isPresent()) {
                throw new IllegalArgumentException("NFC UID already exists: " + nfcUid);
            }
        }

        if ("OPERATOR".equals(role) && pinHash == null && nfcUid == null) {
            throw new IllegalArgumentException("Operator must provide at least pin or nfcUid");
        }

        User user = User.newUser(username, request.fullName(), role, pinHash, nfcUid);
        User u = userRepository.save(user);
        return u.getId().toString();
    }
}

