CREATE TABLE workflow.tasks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_type text NOT NULL,
    target_type text NOT NULL,
    target_id uuid NOT NULL,
    requested_action text NOT NULL,
    status text NOT NULL CHECK (status IN ('OPEN', 'CLAIMED', 'APPROVED', 'REJECTED', 'CANCELLED')),
    priority text NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    created_at timestamptz NOT NULL DEFAULT now(),
    due_at timestamptz,
    assigned_to_user_id uuid REFERENCES identity.users(id),
    assigned_to_role text,
    decision_reason text,
    version bigint NOT NULL DEFAULT 0
);

CREATE INDEX workflow_tasks_target_idx ON workflow.tasks(target_type, target_id);
CREATE INDEX workflow_tasks_status_idx ON workflow.tasks(status);
CREATE INDEX workflow_tasks_assigned_role_idx ON workflow.tasks(assigned_to_role);
