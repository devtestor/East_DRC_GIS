CREATE SCHEMA IF NOT EXISTS pilot;

INSERT INTO identity.roles (code, name, description)
VALUES (
    'PROVINCIAL_LAND_ADMINISTRATOR',
    'Provincial land administrator',
    'Reviews pilot readiness and provincial land-administration operating controls'
)
ON CONFLICT (code) DO NOTHING;

CREATE TABLE pilot.pilot_readiness_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(180) NOT NULL,
    geography_scope VARCHAR(300) NOT NULL,
    pilot_owner VARCHAR(180) NOT NULL,
    planned_start_date DATE,
    planned_end_date DATE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('DRAFT', 'UNDER_REVIEW', 'GO_APPROVED', 'NO_GO', 'CANCELLED')),
    final_decision_reason VARCHAR(500),
    final_decision_task_id UUID,
    requested_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    requested_by VARCHAR(180) NOT NULL,
    decided_by_user_id UUID REFERENCES identity.users(id),
    decided_by VARCHAR(180),
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE pilot.pilot_signoffs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id UUID NOT NULL REFERENCES pilot.pilot_readiness_records(id),
    signoff_type VARCHAR(60) NOT NULL CHECK (signoff_type IN (
        'LEGAL_BOUNDARY',
        'DATA_PROTECTION',
        'SECURITY',
        'OPERATIONS',
        'MIGRATION',
        'TRAINING',
        'SUPPORT',
        'OWNER_APPROVAL'
    )),
    required_role VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    summary VARCHAR(500) NOT NULL,
    workflow_task_id UUID,
    decided_by_user_id UUID REFERENCES identity.users(id),
    decided_by VARCHAR(180),
    decided_at TIMESTAMPTZ,
    created_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    created_by VARCHAR(180) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (pilot_id, signoff_type)
);

CREATE TABLE pilot.pilot_risks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id UUID NOT NULL REFERENCES pilot.pilot_readiness_records(id),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('OPEN', 'MITIGATED', 'ACCEPTED', 'CLOSED')),
    title VARCHAR(180) NOT NULL,
    mitigation_plan VARCHAR(700) NOT NULL,
    blocking_go_live BOOLEAN NOT NULL DEFAULT true,
    created_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    created_by VARCHAR(180) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE pilot.pilot_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id UUID NOT NULL REFERENCES pilot.pilot_readiness_records(id),
    evidence_type VARCHAR(60) NOT NULL CHECK (evidence_type IN (
        'CHARTER',
        'TRAINING_RECORD',
        'MIGRATION_REHEARSAL',
        'SECURITY_SIGNOFF',
        'BACKUP_RESTORE',
        'UAT_RESULT',
        'INCIDENT_RESPONSE',
        'AUTHORIZATION_NOTE',
        'OTHER'
    )),
    reference_type VARCHAR(80) NOT NULL,
    reference_id UUID,
    external_reference VARCHAR(240),
    summary VARCHAR(500) NOT NULL,
    added_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    added_by VARCHAR(180) NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pilot_evidence_has_reference CHECK (reference_id IS NOT NULL OR external_reference IS NOT NULL)
);

CREATE INDEX pilot_records_status_idx ON pilot.pilot_readiness_records (status, created_at DESC);
CREATE INDEX pilot_signoffs_pilot_idx ON pilot.pilot_signoffs (pilot_id, status);
CREATE INDEX pilot_risks_pilot_idx ON pilot.pilot_risks (pilot_id, status, blocking_go_live);
CREATE INDEX pilot_evidence_pilot_idx ON pilot.pilot_evidence (pilot_id, added_at DESC);
