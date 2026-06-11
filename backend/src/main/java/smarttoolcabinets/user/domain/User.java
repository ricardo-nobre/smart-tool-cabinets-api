package smarttoolcabinets.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade base de utilizador de administracao/operacao.
 *
 * Serve para associar atores a sessoes e a eventos de auditoria.
 * Relaciona-se com CabinetAccess (operatorId) e audit log.
 * Evolucao futura: modelo de autenticacao/autorizacao e politicas de roles.
 */
@Entity
@Table(name = "app_user")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "full_name", length = 128)
    private String fullName;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(name = "pin_hash", length = 255)
    private String pinHash;

    @Column(name = "nfc_uid", length = 128)
    private String nfcUid;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected User() {

    }

    public static User newUser(String username, String fullName, String role, String pinHash, String nfcUid) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.username = username;
        user.fullName = fullName;
        user.role = role;
        user.pinHash = pinHash;
        user.nfcUid = nfcUid;
        user.active = true;
        user.createdAt = OffsetDateTime.now();
        return user;
    }

    public UUID getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getPinHash() {
        return pinHash;
    }

    public String getNfcUid() {
        return nfcUid;
    }
}

