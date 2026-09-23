CREATE TABLE pilot.pilot_operational_gates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pilot_id UUID NOT NULL REFERENCES pilot.pilot_readiness_records(id),
    gate_type VARCHAR(60) NOT NULL CHECK (gate_type IN (
        'PRODUCTION_AUTHORIZATION',
        'ENVIRONMENT_PROMOTION',
        'INTEGRATION_ONBOARDING',
        'MIGRATION_REHEARSAL_EXECUTION'
    )),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'PASSED', 'BLOCKED', 'WAIVED')),
    owner_role VARCHAR(80) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    evidence_reference VARCHAR(240),
    created_by_user_id UUID NOT NULL REFERENCES identity.users(id),
    created_by VARCHAR(180) NOT NULL,
    decided_by_user_id UUID REFERENCES identity.users(id),
    decided_by VARCHAR(180),
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (pilot_id, gate_type)
);

CREATE INDEX pilot_operational_gates_pilot_idx ON pilot.pilot_operational_gates (pilot_id, status);
