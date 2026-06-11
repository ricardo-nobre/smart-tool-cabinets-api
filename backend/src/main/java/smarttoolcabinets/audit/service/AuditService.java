package smarttoolcabinets.audit.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.domain.AuditLog;
import smarttoolcabinets.audit.repository.AuditLogRepository;

import java.util.UUID;

/**
 * Service de auditoria transversal da aplicacao.
 *
 * Esta camada recebe eventos de negocio e persiste registos de rastreabilidade.
 * Evolucao futura: politica de formato e normalizacao de auditoria.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Objetivo: registar acao relevante no audit log.
     * Inputs esperados: ator (texto tecnico), acao, tipo de entidade e id opcional.
     * Output esperado: nenhum (efeito de persistencia).
     * Passos logicos a implementar:
     * 1) Validar dados minimos do registo.
     * 2) Construir payload de detalhes em JSON.
     * 3) Persistir AuditLog.
     * 4) Integrar com logging tecnico da aplicacao.
     * Notas: manter formato estavel para analise futura.
     */
    public void logAction(String actor, String action, String entityType, String entityId) {
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("entityType is required");
        }

        AuditEntityType parsedEntityType;
        try {
            parsedEntityType = AuditEntityType.valueOf(entityType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid entityType: " + entityType);
        }

        UUID id = null;
        if (entityId != null && !entityId.trim().isEmpty()) {
            id = UUID.fromString(entityId.trim());
        }

        String normalizedActor = (actor == null || actor.isBlank()) ? "SYSTEM" : actor.trim();
        String actorType = resolveActorType(normalizedActor);
        String actorRef = resolveActorRef(normalizedActor);

        AuditLog auditLog = AuditLog.newEntry(actorType, actorRef, action, parsedEntityType, id, "{}");
        auditLogRepository.save(auditLog);
    }

    public void logAction(String actor, String action, AuditEntityType entityType, String entityId) {
        logAction(actor, action, entityType == null ? null : entityType.name(), entityId);
    }

    private String resolveActorType(String actor) {
        String prefix = actor;
        int separator = actor.indexOf(':');
        if (separator > 0) {
            prefix = actor.substring(0, separator);
        }

        return switch (prefix.toUpperCase()) {
            case "DEVICE", "CABINET" -> "DEVICE";
            case "USER" -> "USER";
            case "SUPERVISOR" -> "SUPERVISOR";
            case "ADMIN" -> "ADMIN";
            default -> "SYSTEM";
        };
    }

    private String resolveActorRef(String actor) {
        int separator = actor.indexOf(':');
        if (separator > 0 && separator < actor.length() - 1) {
            return actor.substring(separator + 1);
        }
        return actor;
    }
}

