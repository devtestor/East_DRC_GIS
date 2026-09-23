CREATE SCHEMA IF NOT EXISTS governance;

CREATE TABLE governance.retention_policies (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    retention_category text NOT NULL UNIQUE,
    description text NOT NULL,
    minimum_retention_days integer NOT NULL CHECK (minimum_retention_days >= 0),
    archival_required boolean NOT NULL DEFAULT true,
    disposal_requires_approval boolean NOT NULL DEFAULT true,
    protected_from_automated_disposal boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE TABLE governance.legal_holds (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type text NOT NULL,
    target_id uuid NOT NULL,
    hold_reason text NOT NULL,
    authority_reference text NOT NULL,
    status text NOT NULL CHECK (status IN ('ACTIVE', 'RELEASED')),
    placed_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    placed_by text NOT NULL,
    placed_at timestamptz NOT NULL DEFAULT now(),
    released_by_user_id uuid REFERENCES identity.users(id),
    released_by text,
    released_at timestamptz,
    release_reason text,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT legal_hold_release_fields CHECK (
        (status = 'ACTIVE' AND released_by_user_id IS NULL AND released_by IS NULL AND released_at IS NULL)
        OR
        (status = 'RELEASED' AND released_by_user_id IS NOT NULL AND released_by IS NOT NULL AND released_at IS NOT NULL)
    )
);

CREATE UNIQUE INDEX legal_holds_one_active_target_idx
    ON governance.legal_holds(target_type, target_id)
    WHERE status = 'ACTIVE';

CREATE INDEX legal_holds_target_idx ON governance.legal_holds(target_type, target_id);

INSERT INTO governance.retention_policies (
    retention_category,
    description,
    minimum_retention_days,
    archival_required,
    disposal_requires_approval,
    protected_from_automated_disposal
) VALUES
    ('LEGAL_RECORD', 'Legal and cadastral evidence retained for long-term institutional recordkeeping.', 3650, true, true, true),
    ('IDENTITY_RECORD', 'Protected personal identity evidence retained only while justified by approved service workflows.', 2555, true, true, true),
    ('FINANCIAL_RECORD', 'Payment and reconciliation evidence retained for audit and treasury reconciliation.', 2555, true, true, true),
    ('OPERATIONAL_RECORD', 'Staff operational records retained for service accountability and audit.', 1095, true, true, false),
    ('PUBLIC_RECORD', 'Public projection records safe for controlled publication after privacy review.', 365, false, true, false)
ON CONFLICT (retention_category) DO NOTHING;
