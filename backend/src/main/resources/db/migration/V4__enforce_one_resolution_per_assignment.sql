ALTER TABLE supervisor_resolution_assignment
    ADD CONSTRAINT uq_supervisor_resolution_assignment_resolution
        UNIQUE (supervisor_resolution_id);

ALTER TABLE supervisor_resolution_assignment
    ADD CONSTRAINT uq_supervisor_resolution_assignment_tool_assignment
        UNIQUE (tool_assignment_id);

DROP INDEX uq_tool_assignment_active_per_tool;

CREATE UNIQUE INDEX uq_tool_assignment_open_per_tool
    ON tool_assignment (tool_id)
    WHERE status IN ('ACTIVE', 'PENDING_REVIEW');
