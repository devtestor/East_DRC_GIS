CREATE TABLE disputes.case_documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id uuid NOT NULL REFERENCES disputes.cases(id),
    document_id uuid NOT NULL REFERENCES documents.documents(id),
    relationship text NOT NULL CHECK (relationship IN ('COMPLAINT', 'IDENTITY_EVIDENCE', 'SURVEY_EVIDENCE', 'COURT_ORDER', 'DECISION', 'OTHER')),
    summary text NOT NULL,
    linked_by_user_id uuid REFERENCES identity.users(id),
    linked_by text NOT NULL,
    linked_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT dispute_case_document_unique UNIQUE (case_id, document_id)
);

CREATE INDEX dispute_case_documents_case_idx ON disputes.case_documents(case_id);
CREATE INDEX dispute_case_documents_document_idx ON disputes.case_documents(document_id);
