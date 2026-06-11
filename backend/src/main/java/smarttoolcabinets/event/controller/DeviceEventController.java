package smarttoolcabinets.event.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttoolcabinets.event.dto.RegisterCabinetEventRequest;
import smarttoolcabinets.event.dto.RegisterCabinetEventResponse;
import smarttoolcabinets.event.service.DeviceEventService;

/**
 * Controller de ingestao de eventos enviados por dispositivo.
 */
@RestController
@RequestMapping("/api/device/cabinet-events")
public class DeviceEventController {

    private final DeviceEventService deviceEventService;

    public DeviceEventController(DeviceEventService deviceEventService) {
        this.deviceEventService = deviceEventService;
    }

    /**
     * Objetivo: registar evento de armario.
     * Inputs esperados: cabinetAccessId, eventType, payload e occurredAt.
     * Output esperado: id do evento criado e estado de processamento.
     * Passos logicos a implementar:
     * 1) Validar estrutura do request.
     * 2) Delegar registo ao service.
     * 3) Mapear respostas de sucesso/falha.
     * Notas: tratar eventos fora de ordem temporal.
     */
    @PostMapping
    public ResponseEntity<RegisterCabinetEventResponse> registerEvent(
            @Valid @RequestBody RegisterCabinetEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceEventService.registerEvent(request));
    }
}

