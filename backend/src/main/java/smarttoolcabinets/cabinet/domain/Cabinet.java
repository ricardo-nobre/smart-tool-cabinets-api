package smarttoolcabinets.cabinet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Entidade base de armario inteligente.
 *
 * Serve para representar o dispositivo fisico no dominio e persistencia.
 * Relaciona-se com CabinetAccess e ferramentas.
 */
@Entity
@Table(name = "cabinet")
public class Cabinet {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 255)
    private String location;

    @Column(name = "api_key_hash", nullable = false, length = 255)
    private String apiKeyHash;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Cabinet() {

    }

    public static Cabinet newCabinet(String code, String name, String location) {
        Cabinet cabinet = new Cabinet();
        cabinet.id = UUID.randomUUID();
        cabinet.code = code;
        cabinet.name = name;
        cabinet.location = location;
        cabinet.apiKeyHash = hashApiKey("DEV-" + code);
        cabinet.active = true;
        cabinet.createdAt = OffsetDateTime.now();
        return cabinet;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public boolean isActive() {
        return active;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public static String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}


