CREATE TABLE workflow.task_evidence_links (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id uuid NOT NULL REFERENCES workflow.tasks(id),
    evidence_type text NOT NULL CHECK (evidence_type IN (
        'DOCUMENT',
        'SURVEY_OBSERVATION',
        'FIELD_PHOTO',
        'BOUNDARY_MARKER',
        'NEIGHBOR_ACKNOWLEDGEMENT',
        'COURT_ORDER',
        'NOTE',
        'OTHER'
    )),
    reference_type text NOT NULL,
    reference_id uuid,
    external_reference text,
    summary text NOT NULL,
    added_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    added_by text NOT NULL,
    added_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT task_evidence_reference_required CHECK (
        reference_id IS NOT NULL OR external_reference IS NOT NULL
    )
);

CREATE INDEX task_evidence_links_task_idx ON workflow.task_evidence_links(task_id);
CREATE INDEX task_evidence_links_reference_idx ON workflow.task_evidence_links(reference_type, reference_id);
CREATE INDEX task_evidence_links_added_by_idx ON workflow.task_evidence_links(added_by_user_id);
