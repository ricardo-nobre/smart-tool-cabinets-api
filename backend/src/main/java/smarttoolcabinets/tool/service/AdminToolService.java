package smarttoolcabinets.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.cabinet.repository.CabinetRepository;
import smarttoolcabinets.tool.domain.Tool;
import smarttoolcabinets.tool.dto.AdminToolCreateRequest;
import smarttoolcabinets.tool.repository.ToolRepository;
import smarttoolcabinets.user.domain.UserRole;

import java.util.UUID;

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

    @Transactional
    public String createTool(AdminToolCreateRequest request) {
        UUID uuid = request.cabinetId();
        if(!cabinetRepository.existsById(uuid)) {
            throw new IllegalArgumentException("Cabinet not found: " + uuid);
        }
        String code = request.tagCode().trim();
        if(toolRepository.existsByTagCode(code)) {
            throw new IllegalArgumentException("Tool tag already exists: " + code);
        }
        Tool tool = Tool.newTool(uuid, code, request.displayName().trim());
        Tool t = toolRepository.save(tool);
        auditService.logAction(
                UserRole.ADMIN,
                "admin",
                "CREATE_TOOL",
                AuditEntityType.TOOL,
                t.getId()
        );
        return t.getId().toString();
    }
}

