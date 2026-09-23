ALTER TABLE workflow.tasks
    ADD COLUMN requested_by text;

UPDATE workflow.tasks
SET requested_by = 'legacy-migration'
WHERE requested_by IS NULL;

ALTER TABLE workflow.tasks
    ALTER COLUMN requested_by SET NOT NULL;

CREATE INDEX workflow_tasks_requested_by_idx ON workflow.tasks(requested_by);
