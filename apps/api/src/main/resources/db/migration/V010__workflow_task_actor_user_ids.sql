ALTER TABLE workflow.tasks
    ADD COLUMN requested_by_user_id uuid REFERENCES identity.users(id),
    ADD COLUMN decided_by_user_id uuid REFERENCES identity.users(id);

UPDATE workflow.tasks task
SET requested_by_user_id = users.id
FROM identity.users users
WHERE lower(task.requested_by) = lower(users.email);

UPDATE workflow.tasks task
SET assigned_to_user_id = users.id
FROM identity.users users
WHERE task.assigned_to_actor IS NOT NULL
  AND lower(task.assigned_to_actor) = lower(users.email);

UPDATE workflow.tasks task
SET decided_by_user_id = users.id
FROM identity.users users
WHERE task.decided_by IS NOT NULL
  AND lower(task.decided_by) = lower(users.email);

CREATE INDEX workflow_tasks_requested_by_user_idx ON workflow.tasks(requested_by_user_id);
CREATE INDEX workflow_tasks_decided_by_user_idx ON workflow.tasks(decided_by_user_id);
