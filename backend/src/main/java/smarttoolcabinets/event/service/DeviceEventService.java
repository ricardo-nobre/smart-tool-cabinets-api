package smarttoolcabinets.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.service.AuditService;
import smarttoolcabinets.event.domain.CabinetEvent;
import smarttoolcabinets.event.dto.RegisterCabinetEventRequest;
import smarttoolcabinets.event.dto.RegisterCabinetEventResponse;
import smarttoolcabinets.event.repository.CabinetEventRepository;
import smarttoolcabinets.session.domain.Session;
import smarttoolcabinets.session.repository.SessionRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Service de ingestao de eventos emitidos pelo armario.
 */
@Service
public class DeviceEventService {

    private final SessionRepository sessionRepository;
    private final CabinetEventRepository cabinetEventRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public DeviceEventService(SessionRepository sessionRepository, CabinetEventRepository cabinetEventRepository, AuditService auditService,  ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.cabinetEventRepository = cabinetEventRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * Objetivo: registar um evento de dispositivo num CabinetAccess.
     * Inputs esperados: cabinetAccessId, eventType, payload e timestamp.
     * Output esperado: identificador do evento persistido e estado de processamento.
     * Passos logicos a implementar:
     * 1) Validar CabinetAccess ativo e permissao para receber eventos.
     * 2) Validar schema do payload por tipo de evento.
     * 3) Persistir evento e produzir auditoria.
     * 4) Encadear atualizacoes necessarias de snapshot, se aplicavel.
     * Notas: manter idempotencia para eventos repetidos do dispositivo.
     */
    @Transactional
    public RegisterCabinetEventResponse registerEvent(RegisterCabinetEventRequest request) {
        UUID cabinetAccessId = request.cabinetAccessId();
        String eventType = request.eventType().trim().toUpperCase();
        String payload = "{}";
        if (request.payload() != null && !request.payload().isEmpty()) {
            try {
                payload = objectMapper.writeValueAsString(request.payload());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid event payload");
            }
        }
        Optional<Session> session = sessionRepository.findById(cabinetAccessId);
        if(session.isEmpty()) {
            throw new IllegalArgumentException("CabinetAccess not found: " + cabinetAccessId);
        }
        Session s = session.get();
        if (!"OPEN".equals(s.getStatus())) {
            throw new IllegalStateException("CabinetAccess is not open: " + cabinetAccessId);
        }

        CabinetEvent cabinetEvent = CabinetEvent.cabinetEvent(cabinetAccessId, eventType, payload, request.occurredAt());
        CabinetEvent e = cabinetEventRepository.save(cabinetEvent);
        auditService.logAction("CABINET_ACCESS:" + cabinetAccessId, "REGISTER_EVENT:" + eventType, AuditEntityType.EVENT, e.getId().toString());
        return new RegisterCabinetEventResponse(e.getId(), "REGISTERED");

    }
}

