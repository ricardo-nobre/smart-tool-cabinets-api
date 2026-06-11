package smarttoolcabinets.cabinet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade base de armario inteligente.
 *
 * Serve para representar o dispositivo fisico no dominio e persistencia.
 * Relaciona-se com sessoes e ferramentas.
 * Evolucao futura: regras de ciclo de vida (ativacao/desativacao) e seguranca da API key.
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

    @Column(name = "api_key_hash", length = 255)
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
        cabinet.apiKeyHash = null;
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
}


