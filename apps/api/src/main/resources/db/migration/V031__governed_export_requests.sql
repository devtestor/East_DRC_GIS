CREATE TABLE governance.export_requests (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id uuid NOT NULL REFERENCES documents.documents(id),
    purpose text NOT NULL,
    redaction_planned boolean NOT NULL DEFAULT false,
    status text NOT NULL CHECK (status IN (
        'BLOCKED',
        'PENDING_REVIEW',
        'APPROVED',
        'REJECTED'
    )),
    redaction_required boolean NOT NULL DEFAULT false,
    approval_required boolean NOT NULL DEFAULT true,
    blocker_summary text NOT NULL DEFAULT '',
    workflow_task_id uuid REFERENCES workflow.tasks(id),
    requested_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    requested_by text NOT NULL,
    requested_at timestamptz NOT NULL DEFAULT now(),
    decided_by_user_id uuid REFERENCES identity.users(id),
    decided_by text,
    decided_at timestamptz,
    decision_reason text,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT export_request_decision_fields CHECK (
        (status IN ('BLOCKED', 'PENDING_REVIEW') AND decided_by_user_id IS NULL AND decided_by IS NULL AND decided_at IS NULL)
        OR
        (status IN ('APPROVED', 'REJECTED') AND decided_by_user_id IS NOT NULL AND decided_by IS NOT NULL AND decided_at IS NOT NULL)
    )
);

CREATE INDEX export_requests_document_idx ON governance.export_requests(document_id);
CREATE INDEX export_requests_status_idx ON governance.export_requests(status, requested_at);
CREATE UNIQUE INDEX export_requests_workflow_task_unique_idx
    ON governance.export_requests(workflow_task_id)
    WHERE workflow_task_id IS NOT NULL;
