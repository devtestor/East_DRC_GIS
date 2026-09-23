ALTER TABLE workflow.tasks
    ADD COLUMN assigned_to_actor text,
    ADD COLUMN claimed_at timestamptz;

CREATE INDEX workflow_tasks_assigned_actor_idx ON workflow.tasks(assigned_to_actor);
CREATE INDEX workflow_tasks_claimed_at_idx ON workflow.tasks(claimed_at);
