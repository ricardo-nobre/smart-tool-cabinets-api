CREATE TABLE cabinet (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    location VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(128),
    role VARCHAR(32) NOT NULL,
    pin_hash VARCHAR(255),
    nfc_uid VARCHAR(128),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tool (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL,
    tag_code VARCHAR(128) NOT NULL UNIQUE,
    display_name VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tool_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinet(id)
);

CREATE TABLE cabinet_access (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL,
    operator_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cabinet_access_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinet(id),
    CONSTRAINT fk_cabinet_access_operator FOREIGN KEY (operator_id) REFERENCES app_user(id)
);

CREATE TABLE cabinet_event (
    id UUID PRIMARY KEY,
    cabinet_access_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload_json TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cabinet_event_cabinet_access FOREIGN KEY (cabinet_access_id) REFERENCES cabinet_access(id)
);

CREATE TABLE inventory_snapshot (
    id UUID PRIMARY KEY,
    cabinet_access_id UUID NOT NULL,
    snapshot_type VARCHAR(16) NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL,
    source VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_inventory_snapshot_cabinet_access FOREIGN KEY (cabinet_access_id) REFERENCES cabinet_access(id)
);

CREATE TABLE inventory_snapshot_item (
    id UUID PRIMARY KEY,
    snapshot_id UUID NOT NULL,
    tag_code VARCHAR(128) NOT NULL,
    tool_id UUID,
    recognized BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_inventory_snapshot_item_snapshot FOREIGN KEY (snapshot_id) REFERENCES inventory_snapshot(id),
    CONSTRAINT fk_inventory_snapshot_item_tool FOREIGN KEY (tool_id) REFERENCES tool(id)
);

CREATE TABLE tool_assignment (
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
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tool_assignment_tool FOREIGN KEY (tool_id) REFERENCES tool(id),
    CONSTRAINT fk_tool_assignment_operator FOREIGN KEY (operator_id) REFERENCES app_user(id),
    CONSTRAINT fk_tool_assignment_origin_cabinet FOREIGN KEY (origin_cabinet_id) REFERENCES cabinet(id),
    CONSTRAINT fk_tool_assignment_origin_access FOREIGN KEY (origin_cabinet_access_id) REFERENCES cabinet_access(id),
    CONSTRAINT fk_tool_assignment_returned_cabinet FOREIGN KEY (returned_to_cabinet_id) REFERENCES cabinet(id),
    CONSTRAINT fk_tool_assignment_returned_access FOREIGN KEY (returned_via_cabinet_access_id) REFERENCES cabinet_access(id)
);

CREATE TABLE supervisor_resolution (
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

CREATE TABLE supervisor_resolution_assignment (
    supervisor_resolution_id UUID NOT NULL,
    tool_assignment_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (supervisor_resolution_id, tool_assignment_id),
    CONSTRAINT fk_resolution_assignment_resolution FOREIGN KEY (supervisor_resolution_id) REFERENCES supervisor_resolution(id),
    CONSTRAINT fk_resolution_assignment_assignment FOREIGN KEY (tool_assignment_id) REFERENCES tool_assignment(id)
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    actor_type VARCHAR(32) NOT NULL,
    actor_ref VARCHAR(128),
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_app_user_nfc_uid_not_null
    ON app_user (nfc_uid)
    WHERE nfc_uid IS NOT NULL;

CREATE UNIQUE INDEX uq_cabinet_access_open_per_cabinet
    ON cabinet_access (cabinet_id)
    WHERE status = 'OPEN';

CREATE UNIQUE INDEX uq_inventory_snapshot_item_snapshot_tag
    ON inventory_snapshot_item (snapshot_id, tag_code);

CREATE UNIQUE INDEX uq_tool_assignment_active_per_tool
    ON tool_assignment (tool_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_tool_cabinet_id
    ON tool (cabinet_id);

CREATE INDEX idx_cabinet_access_cabinet_status
    ON cabinet_access (cabinet_id, status);

CREATE INDEX idx_cabinet_access_operator_opened_at
    ON cabinet_access (operator_id, opened_at);

CREATE INDEX idx_cabinet_event_cabinet_access_id
    ON cabinet_event (cabinet_access_id);

CREATE INDEX idx_inventory_snapshot_cabinet_access_id
    ON inventory_snapshot (cabinet_access_id);

CREATE INDEX idx_inventory_snapshot_item_snapshot_id
    ON inventory_snapshot_item (snapshot_id);

CREATE INDEX idx_inventory_snapshot_item_tool_id
    ON inventory_snapshot_item (tool_id);

CREATE INDEX idx_tool_assignment_operator_status
    ON tool_assignment (operator_id, status);

CREATE INDEX idx_supervisor_resolution_operator_decision_at
    ON supervisor_resolution (operator_id, decision_at);

CREATE INDEX idx_audit_log_entity_type_entity_id
    ON audit_log (entity_type, entity_id);
