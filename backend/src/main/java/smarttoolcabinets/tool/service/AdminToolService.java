package smarttoolcabinets.tool.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.dto.AdminToolCreateRequest;
import smarttoolcabinets.tool.repository.ToolRepository;

import java.util.UUID;

/**
 * Service administrativo para gestao de ferramentas.
 *
 * Evolucao futura: validacoes por armario e regras de ciclo de vida da ferramenta.
 */
@Service
public class AdminToolService {

    private final ToolRepository toolRepository;
    private final CabinetRepository cabinetRepository;
    private final AuditService auditService;

    public AdminToolService(ToolRepository toolRepository, CabinetRepository cabinetRepository, AuditService auditService) {
        this.toolRepository = toolRepository;
        this.cabinetRepository = cabinetRepository;
        this.auditService = auditService;
    }

    /**
     * Objetivo: criar ferramenta num armario.
     * Inputs esperados: cabinetId, tag e nome da ferramenta.
     * Output esperado: id da ferramenta criada.
     * Passos logicos a implementar:
     * 1) Validar existencia do armario.
     * 2) Validar tag unica globalmente no sistema.
     * 3) Persistir entidade Tool.
     * 4) Registar auditoria.
     * Notas: garantir consistencia com snapshots futuros.
     */
    public String createTool(AdminToolCreateRequest request) {
        UUID uuid = request.cabinetId();
        if(!cabinetRepository.existsById(uuid)) {
            throw new IllegalArgumentException("Cabinet not found: " + uuid);
        }
        String code = request.tagCode().trim();
        if(toolRepository.existsByTagCode(code)) {
            throw new IllegalArgumentException("Tool tag already exists: " + code);
        }
        String serialNumber = request.serialNumber() == null ? null : request.serialNumber().trim();
        Tool tool = Tool.newTool(uuid, code, request.displayName().trim(), request.typeCode(), serialNumber);
        Tool t = toolRepository.save(tool);
        auditService.logAction("admin", "create_tool", AuditEntityType.TOOL, t.getId().toString());
        return t.getId().toString();
    }
}

