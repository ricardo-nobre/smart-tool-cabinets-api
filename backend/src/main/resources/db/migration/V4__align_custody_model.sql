-- Fase estrutural: alinhar backend com dominio de custodia (docs v2)

-- 1) Session -> CabinetAccess
ALTER TABLE IF EXISTS session RENAME TO cabinet_access;

ALTER TABLE IF EXISTS cabinet_access
    RENAME COLUMN opened_by_user_id TO operator_id;

ALTER TABLE IF EXISTS cabinet_access
    ALTER COLUMN operator_id SET NOT NULL;

-- 2) Eventos e snapshots referenciam cabinet_access
ALTER TABLE IF EXISTS cabinet_event
    RENAME COLUMN session_id TO cabinet_access_id;

ALTER TABLE IF EXISTS inventory_snapshot
    RENAME COLUMN session_id TO cabinet_access_id;

ALTER TABLE IF EXISTS inventory_snapshot
    ADD COLUMN IF NOT EXISTS snapshot_type VARCHAR(16);

UPDATE inventory_snapshot
SET snapshot_type = COALESCE(snapshot_type, 'EXTRA');

ALTER TABLE IF EXISTS inventory_snapshot
    ALTER COLUMN snapshot_type SET NOT NULL;

-- 3) Tool com atributos de dominio faltantes
ALTER TABLE IF EXISTS tool
    ADD COLUMN IF NOT EXISTS type_code INTEGER;

UPDATE tool
SET type_code = COALESCE(type_code, 0);

ALTER TABLE IF EXISTS tool
    ALTER COLUMN type_code SET NOT NULL;

ALTER TABLE IF EXISTS tool
    ADD COLUMN IF NOT EXISTS serial_number VARCHAR(128);

-- 4) Snapshot items por tag observada (recognized/unknown)
ALTER TABLE IF EXISTS inventory_snapshot_item
    DROP COLUMN IF EXISTS present;

UPDATE inventory_snapshot_item isi
SET tag_code = t.tag_code
FROM tool t
WHERE isi.tool_id = t.id
  AND (isi.tag_code IS NULL OR btrim(isi.tag_code) = '');

ALTER TABLE IF EXISTS inventory_snapshot_item
    ALTER COLUMN tag_code SET NOT NULL;

-- 5) Estruturas novas do dominio de custodia
CREATE TABLE IF NOT EXISTS tool_assignment (
    id UUID PRIMARY KEY,
    tool_id UUID NOT NULL,
    operator_id UUID NOT NULL,
    origin_cabinet_id UUID NOT NULL,
    origin_cabinet_access_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    returned_at TIMESTAMPTZ,
    returned_to_cabinet_id UUID,
    returned_via_cabinet_access_id UUID,
    status VARCHAR(32) NOT NULL,
    pending_end_of_day BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tool_assignment_tool FOREIGN KEY (tool_id) REFERENCES tool(id),
    CONSTRAINT fk_tool_assignment_operator FOREIGN KEY (operator_id) REFERENCES app_user(id),
    CONSTRAINT fk_tool_assignment_origin_cabinet FOREIGN KEY (origin_cabinet_id) REFERENCES cabinet(id),
    CONSTRAINT fk_tool_assignment_origin_access FOREIGN KEY (origin_cabinet_access_id) REFERENCES cabinet_access(id),
    CONSTRAINT fk_tool_assignment_returned_cabinet FOREIGN KEY (returned_to_cabinet_id) REFERENCES cabinet(id),
    CONSTRAINT fk_tool_assignment_returned_access FOREIGN KEY (returned_via_cabinet_access_id) REFERENCES cabinet_access(id)
);

CREATE TABLE IF NOT EXISTS supervisor_resolution (
    id UUID PRIMARY KEY,
    operator_id UUID NOT NULL,
    supervisor_id UUID NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    report_text TEXT NOT NULL,
    decision_at TIMESTAMPTZ NOT NULL,
    allow_exit BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_supervisor_resolution_operator FOREIGN KEY (operator_id) REFERENCES app_user(id),
    CONSTRAINT fk_supervisor_resolution_supervisor FOREIGN KEY (supervisor_id) REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS supervisor_resolution_assignment (
    supervisor_resolution_id UUID NOT NULL,
    tool_assignment_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (supervisor_resolution_id, tool_assignment_id),
    CONSTRAINT fk_resolution_assignment_resolution FOREIGN KEY (supervisor_resolution_id) REFERENCES supervisor_resolution(id),
    CONSTRAINT fk_resolution_assignment_assignment FOREIGN KEY (tool_assignment_id) REFERENCES tool_assignment(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_cabinet_access_open_per_cabinet
    ON cabinet_access (cabinet_id)
    WHERE status = 'OPEN';

CREATE UNIQUE INDEX IF NOT EXISTS uq_tool_assignment_active_per_tool
    ON tool_assignment (tool_id)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_tool_assignment_operator_status
    ON tool_assignment (operator_id, status);

CREATE INDEX IF NOT EXISTS idx_cabinet_access_operator_opened_at
    ON cabinet_access (operator_id, opened_at);

CREATE INDEX IF NOT EXISTS idx_supervisor_resolution_operator_decision_at
    ON supervisor_resolution (operator_id, decision_at);

