package smarttoolcabinets.cabinet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.cabinet.dto.AdminCabinetCreateRequest;
import smarttoolcabinets.cabinet.service.AdminCabinetService;

import java.util.Map;

/**
 * Controller admin para gestao de armarios.
 *
 * Define endpoints iniciais para operacoes administrativas de cabinet.
 * Evolucao futura: seguranca por role ADMIN e validacoes de negocio.
 */
@RestController
@RequestMapping("/api/admin/cabinets")
public class AdminCabinetController {

    private final AdminCabinetService adminCabinetService;

    public AdminCabinetController(AdminCabinetService adminCabinetService) {
        this.adminCabinetService = adminCabinetService;
    }

    /**
     * Objetivo: criar armario por endpoint admin.
     * Inputs esperados: code, name e location opcional.
     * Output esperado: id do armario criado.
     * Passos logicos a implementar:
     * 1) Validar payload.
     * 2) Delegar no service.
     * 3) Devolver identificador criado.
     * Notas: mapear conflitos de codigo unico para 409.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createCabinet(@Valid @RequestBody AdminCabinetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("cabinetId", adminCabinetService.createCabinet(request)));
    }
}

