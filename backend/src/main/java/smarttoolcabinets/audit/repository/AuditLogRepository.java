package smarttoolcabinets.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.audit.domain.AuditLog;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}

