ALTER TABLE documents.document_versions
    ADD COLUMN safety_status text NOT NULL DEFAULT 'AVAILABLE',
    ADD COLUMN safety_reason text,
    ADD COLUMN safety_reviewed_by_user_id uuid REFERENCES identity.users(id),
    ADD COLUMN safety_reviewed_by text,
    ADD COLUMN safety_reviewed_at timestamptz,
    ADD CONSTRAINT document_version_safety_status_check CHECK (safety_status IN (
        'AVAILABLE',
        'QUARANTINED'
    )),
    ADD CONSTRAINT document_version_safety_review_complete_check CHECK (
        (safety_reviewed_at IS NULL AND safety_reviewed_by_user_id IS NULL AND safety_reviewed_by IS NULL)
        OR
        (safety_reviewed_at IS NOT NULL AND safety_reviewed_by_user_id IS NOT NULL AND safety_reviewed_by IS NOT NULL)
    );

CREATE INDEX document_versions_safety_status_idx ON documents.document_versions(safety_status);
