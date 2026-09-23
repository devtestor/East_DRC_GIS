CREATE TABLE disputes.cases (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id uuid NOT NULL REFERENCES parcels.parcels(id),
    case_type text NOT NULL CHECK (
        case_type IN ('BOUNDARY_DISPUTE', 'OWNERSHIP_DISPUTE', 'COMPETING_CLAIM', 'FRAUD_ALLEGATION', 'ADMINISTRATIVE_APPEAL', 'OTHER')
    ),
    status text NOT NULL CHECK (
        status IN ('OPEN', 'UNDER_REVIEW', 'HEARING', 'DECIDED', 'RESOLVED', 'CLOSED', 'REOPENED')
    ),
    summary text NOT NULL,
    source text NOT NULL,
    authority_reference text,
    opened_by_user_id uuid REFERENCES identity.users(id),
    opened_by text NOT NULL,
    opened_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE INDEX dispute_cases_parcel_idx ON disputes.cases(parcel_id);
CREATE INDEX dispute_cases_status_idx ON disputes.cases(status);
