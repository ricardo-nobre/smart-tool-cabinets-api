package smarttoolcabinets.user.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.user.dto.AdminUserCreateRequest;
import smarttoolcabinets.user.service.AdminUserService;

import java.util.Map;

/**
 * Controller admin para gestao de utilizadores.
 *
 * Exposicao base para criacao de utilizadores administrativos/operacionais.
 * Evolucao futura: politicas de permissao e validacoes detalhadas de role.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * Objetivo: criar utilizador via endpoint admin.
     * Inputs esperados: username, fullName e role.
     * Output esperado: id do utilizador criado.
     * Passos logicos a implementar:
     * 1) Validar dados de entrada.
     * 2) Delegar criacao ao service.
     * 3) Devolver resultado normalizado.
     * Notas: aplicar restricoes de unicidade de username.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("userId", adminUserService.createUser(request)));
    }
}

