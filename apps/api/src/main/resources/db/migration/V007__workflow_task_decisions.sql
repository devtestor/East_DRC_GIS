ALTER TABLE workflow.tasks
    ADD COLUMN decided_at timestamptz,
    ADD COLUMN decided_by text;

CREATE INDEX workflow_tasks_decided_at_idx ON workflow.tasks(decided_at);
