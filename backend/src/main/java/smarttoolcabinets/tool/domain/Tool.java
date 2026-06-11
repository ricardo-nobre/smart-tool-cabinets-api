package smarttoolcabinets.tool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidade base de ferramenta registada no sistema.
 *
 * Serve para mapear ferramenta esperada em cada armario.
 * Relaciona-se com snapshots de inventario e eventos operacionais.
 * Evolucao futura: politicas de estado funcional e ciclo de vida da ferramenta.
 */
@Entity
@Table(name = "tool")
public class Tool {

    @Id
    private UUID id;

    @Column(name = "cabinet_id", nullable = false)
    private UUID cabinetId;

    @Column(name = "tag_code", nullable = false, length = 128)
    private String tagCode;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "type_code", nullable = false)
    private int typeCode;

    @Column(name = "serial_number", length = 128)
    private String serialNumber;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Tool() {

    }

    public static Tool newTool(UUID cabinetId, String tagCode, String displayName, int typeCode, String serialNumber) {
        Tool tool = new Tool();
        tool.id = UUID.randomUUID();
        tool.cabinetId = cabinetId;
        tool.tagCode = tagCode;
        tool.displayName = displayName;
        tool.typeCode = typeCode;
        tool.serialNumber = serialNumber;
        tool.active = true;
        tool.createdAt = OffsetDateTime.now();
        return tool;
    }

    public UUID getId() {
        return id;
    }

    public String getTagCode() {
        return tagCode;
    }

    public String getDisplayName() {
        return displayName;
    }
}

