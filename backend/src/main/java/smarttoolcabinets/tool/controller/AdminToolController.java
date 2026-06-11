package smarttoolcabinets.tool.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.tool.dto.AdminToolCreateRequest;
import smarttoolcabinets.tool.service.AdminToolService;

import java.util.Map;

/**
 * Controller admin para gestao de ferramentas.
 *
 * Define o contrato inicial para criacao de ferramentas.
 * Evolucao futura: autorizacao por role e fluxos de manutencao completos.
 */
@RestController
@RequestMapping("/api/admin/tools")
public class AdminToolController {

    private final AdminToolService adminToolService;

    public AdminToolController(AdminToolService adminToolService) {
        this.adminToolService = adminToolService;
    }

    /**
     * Objetivo: criar ferramenta por endpoint admin.
     * Inputs esperados: cabinetId, tagCode, displayName, typeCode e serialNumber opcional.
     * Output esperado: id da ferramenta criada.
     * Passos logicos a implementar:
     * 1) Validar payload.
     * 2) Delegar criacao ao service.
     * 3) Mapear resultado para resposta padronizada.
     * Notas: tratar conflito de tag com resposta apropriada.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createTool(@Valid @RequestBody AdminToolCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("toolId", adminToolService.createTool(request)));
    }
}

