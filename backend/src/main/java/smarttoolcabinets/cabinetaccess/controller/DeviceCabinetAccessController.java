package smarttoolcabinets.cabinetaccess.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttoolcabinets.inventory.dto.CreateSnapshotRequest;
import smarttoolcabinets.inventory.dto.CreateSnapshotResponse;
import smarttoolcabinets.inventory.service.InventoryService;
import smarttoolcabinets.cabinetaccess.dto.CloseCabinetAccessResponse;
import smarttoolcabinets.cabinetaccess.dto.OpenCabinetAccessRequest;
import smarttoolcabinets.cabinetaccess.dto.OpenCabinetAccessResponse;
import smarttoolcabinets.cabinetaccess.service.DeviceCabinetAccessService;

/**
 * Controller do fluxo de CabinetAccess para endpoints de dispositivo.
 */
@RestController
@RequestMapping("/api/device/cabinet-accesses")
public class DeviceCabinetAccessController {

    private final DeviceCabinetAccessService deviceCabinetAccessService;
    private final InventoryService inventoryService;

    public DeviceCabinetAccessController(DeviceCabinetAccessService deviceCabinetAccessService,  InventoryService inventoryService) {
        this.deviceCabinetAccessService = deviceCabinetAccessService;
        this.inventoryService = inventoryService;
    }

    /**
     * Abre CabinetAccess com operatorId previamente autenticado.
     */
    @PostMapping
    public ResponseEntity<OpenCabinetAccessResponse> openCabinetAccess(
                @Valid @RequestBody OpenCabinetAccessRequest request) {
            return ResponseEntity.status(HttpStatus.CREATED).body(deviceCabinetAccessService.openCabinetAccess(request));
        }

    /**
     * Fecha CabinetAccess e devolve resultado operacional de custodia.
     * O fecho fisico do armario ocorre sempre.
     */
    @PostMapping("/{cabinetAccessId}/close")
    public ResponseEntity<CloseCabinetAccessResponse> close(@PathVariable("cabinetAccessId") String cabinetAccessId){
        return ResponseEntity.ok(deviceCabinetAccessService.closeCabinetAccess(cabinetAccessId));
    }

    @PostMapping("/{cabinetAccessId}/snapshots")
    public ResponseEntity<CreateSnapshotResponse> createSnapshot(@PathVariable("cabinetAccessId") String cabinetAccessId, @Valid @RequestBody CreateSnapshotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createSnapshot(cabinetAccessId, request));
    }
}

