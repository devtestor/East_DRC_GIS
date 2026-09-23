ALTER TABLE disputes.cases
    ADD COLUMN decision_summary text,
    ADD COLUMN decided_by_user_id uuid REFERENCES identity.users(id),
    ADD COLUMN decided_by text,
    ADD COLUMN decided_at timestamptz;

CREATE TABLE disputes.hearings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id uuid NOT NULL REFERENCES disputes.cases(id),
    scheduled_for timestamptz NOT NULL,
    venue text NOT NULL,
    notes text,
    status text NOT NULL CHECK (status IN ('SCHEDULED', 'HELD', 'CANCELLED')),
    scheduled_by_user_id uuid REFERENCES identity.users(id),
    scheduled_by text NOT NULL,
    scheduled_at timestamptz NOT NULL DEFAULT now(),
    held_at timestamptz,
    version bigint NOT NULL DEFAULT 0
);

CREATE INDEX dispute_hearings_case_idx ON disputes.hearings(case_id);

CREATE TABLE disputes.appeals (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id uuid NOT NULL REFERENCES disputes.cases(id),
    appeal_type text NOT NULL CHECK (appeal_type IN ('ADMINISTRATIVE_APPEAL', 'CORRECTION_REQUEST', 'REOPENING_REQUEST', 'OTHER')),
    grounds text NOT NULL,
    status text NOT NULL CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'DECIDED', 'WITHDRAWN')),
    filed_by_user_id uuid REFERENCES identity.users(id),
    filed_by text NOT NULL,
    filed_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE INDEX dispute_appeals_case_idx ON disputes.appeals(case_id);
