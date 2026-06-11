package smarttoolcabinets.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.audit.domain.AuditLog;

import java.util.UUID;

/**
 * Repositorio JPA para AuditLog.
 *
 * Regista eventos de auditoria de modo persistente.
 * Evolucao futura: pesquisa por ator, acao e periodo temporal.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}

