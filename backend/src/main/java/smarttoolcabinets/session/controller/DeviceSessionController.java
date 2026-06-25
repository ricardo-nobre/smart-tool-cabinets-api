package smarttoolcabinets.session.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttoolcabinets.inventory.dto.CreateSnapshotRequest;
import smarttoolcabinets.inventory.dto.CreateSnapshotResponse;
import smarttoolcabinets.inventory.service.InventoryService;
import smarttoolcabinets.session.dto.CloseSessionResponse;
import smarttoolcabinets.session.dto.OpenSessionRequest;
import smarttoolcabinets.session.dto.OpenSessionResponse;
import smarttoolcabinets.session.service.DeviceSessionService;

/**
 * Controller do fluxo de CabinetAccess para endpoints de dispositivo.
 */
@RestController
@RequestMapping("/api/device/cabinet-accesses")
public class DeviceSessionController {

    private final DeviceSessionService deviceSessionService;
    private final InventoryService inventoryService;

    public DeviceSessionController(DeviceSessionService deviceSessionService,  InventoryService inventoryService) {
        this.deviceSessionService = deviceSessionService;
        this.inventoryService = inventoryService;
    }

    /**
     * Abre CabinetAccess com operatorId previamente autenticado.
     */
    @PostMapping
    public ResponseEntity<OpenSessionResponse> openSession(
                @Valid @RequestBody OpenSessionRequest request) {
            return ResponseEntity.status(HttpStatus.CREATED).body(deviceSessionService.openSession(request));
        }

    /**
     * Fecha CabinetAccess e devolve resultado operacional de custodia.
     * O fecho fisico do armario ocorre sempre.
     */
    @PostMapping("/{cabinetAccessId}/close")
    public ResponseEntity<CloseSessionResponse> close(@PathVariable("cabinetAccessId") String cabinetAccessId){
        return ResponseEntity.ok(deviceSessionService.closeSession(cabinetAccessId));
    }

    @PostMapping("/{cabinetAccessId}/snapshots")
    public ResponseEntity<CreateSnapshotResponse> createSnapshot(@PathVariable("cabinetAccessId") String cabinetAccessId, @Valid @RequestBody CreateSnapshotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createSnapshot(cabinetAccessId, request));
    }
}

