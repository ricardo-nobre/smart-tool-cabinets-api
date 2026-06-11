-- Schema inicial minimo para arrancar desenvolvimento.
-- TODO: rever constraints de negocio e indices conforme os casos de uso.

CREATE TABLE cabinet (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    location VARCHAR(255),
    api_key_hash VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(128),
    role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tool (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL,
    tag_code VARCHAR(128) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tool_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinet(id)
);

CREATE TABLE session (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL,
    opened_by_user_id UUID,
    status VARCHAR(32) NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_session_cabinet FOREIGN KEY (cabinet_id) REFERENCES cabinet(id),
    CONSTRAINT fk_session_opened_by_user FOREIGN KEY (opened_by_user_id) REFERENCES app_user(id)
);

CREATE TABLE cabinet_event (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload_json TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cabinet_event_session FOREIGN KEY (session_id) REFERENCES session(id)
);

CREATE TABLE inventory_snapshot (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL,
    source VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_inventory_snapshot_session FOREIGN KEY (session_id) REFERENCES session(id)
);

CREATE TABLE inventory_snapshot_item (
    id UUID PRIMARY KEY,
    snapshot_id UUID NOT NULL,
    tool_id UUID NOT NULL,
    present BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_inventory_item_snapshot FOREIGN KEY (snapshot_id) REFERENCES inventory_snapshot(id),
    CONSTRAINT fk_inventory_item_tool FOREIGN KEY (tool_id) REFERENCES tool(id)
);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    actor VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID,
    details_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

ALTER TABLE tool
    ADD CONSTRAINT uq_tool_cabinet_tag UNIQUE (cabinet_id, tag_code);

ALTER TABLE inventory_snapshot_item
    ADD CONSTRAINT uq_inventory_snapshot_item_snapshot_tool UNIQUE (snapshot_id, tool_id);

CREATE UNIQUE INDEX uq_session_open_per_cabinet ON session (cabinet_id)
    WHERE status = 'OPEN';

CREATE INDEX idx_session_cabinet_status ON session (cabinet_id, status);
CREATE INDEX idx_tool_cabinet_id ON tool (cabinet_id);
CREATE INDEX idx_cabinet_event_session_id ON cabinet_event (session_id);
CREATE INDEX idx_inventory_snapshot_session_id ON inventory_snapshot (session_id);
CREATE INDEX idx_inventory_snapshot_item_snapshot_id ON inventory_snapshot_item (snapshot_id);
CREATE INDEX idx_inventory_snapshot_item_tool_id ON inventory_snapshot_item (tool_id);
CREATE INDEX idx_audit_log_entity_type_entity_id ON audit_log (entity_type, entity_id);

