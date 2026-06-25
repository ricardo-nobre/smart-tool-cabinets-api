-- Align audit_log with the current AuditLog entity.
-- V1 created actor as NOT NULL; V3 migrated data to actor_type/actor_ref.
ALTER TABLE audit_log
    DROP COLUMN IF EXISTS actor;
