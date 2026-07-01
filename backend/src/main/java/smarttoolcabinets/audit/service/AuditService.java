package smarttoolcabinets.audit.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.audit.domain.AuditEntityType;
import smarttoolcabinets.audit.domain.AuditLog;
import smarttoolcabinets.audit.repository.AuditLogRepository;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String actorType, String actorRef, String action, AuditEntityType entityType, UUID entityId) {
        if (entityType == null) {
            throw new IllegalArgumentException("entityType is required");
        }

        AuditLog auditLog = AuditLog.newEntry(actorType, actorRef, action, entityType, entityId);
        auditLogRepository.save(auditLog);
    }
}

