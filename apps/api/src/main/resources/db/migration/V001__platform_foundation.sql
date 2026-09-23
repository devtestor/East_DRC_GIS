CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS administration;
CREATE SCHEMA IF NOT EXISTS parcels;
CREATE SCHEMA IF NOT EXISTS cadastre;
CREATE SCHEMA IF NOT EXISTS parties;
CREATE SCHEMA IF NOT EXISTS rights;
CREATE SCHEMA IF NOT EXISTS transactions;
CREATE SCHEMA IF NOT EXISTS workflow;
CREATE SCHEMA IF NOT EXISTS documents;
CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS disputes;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS integrations;

CREATE TABLE identity.organizations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_type text NOT NULL,
    legal_name text NOT NULL,
    status text NOT NULL CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'EXPIRED')),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE TABLE identity.users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email text NOT NULL,
    display_name text NOT NULL,
    password_hash text NOT NULL,
    status text NOT NULL CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'LOCKED')),
    mfa_required boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE TABLE identity.roles (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code text NOT NULL UNIQUE,
    name text NOT NULL,
    description text NOT NULL DEFAULT ''
);

CREATE TABLE identity.permissions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code text NOT NULL UNIQUE,
    description text NOT NULL DEFAULT ''
);

CREATE TABLE identity.role_permissions (
    role_id uuid NOT NULL REFERENCES identity.roles(id),
    permission_id uuid NOT NULL REFERENCES identity.permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE identity.organization_memberships (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES identity.users(id),
    organization_id uuid NOT NULL REFERENCES identity.organizations(id),
    role_id uuid NOT NULL REFERENCES identity.roles(id),
    province_code text,
    jurisdiction_path text,
    status text NOT NULL CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'SUSPENDED', 'EXPIRED')),
    starts_at timestamptz NOT NULL DEFAULT now(),
    ends_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT membership_unique_active_scope UNIQUE (user_id, organization_id, role_id, province_code, jurisdiction_path)
);

CREATE TABLE audit.audit_events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_time timestamptz NOT NULL DEFAULT now(),
    correlation_id text NOT NULL,
    actor_user_id uuid,
    actor_organization_id uuid,
    actor_ip inet,
    actor_user_agent text,
    action text NOT NULL,
    target_type text NOT NULL,
    target_id text,
    previous_value jsonb,
    new_value jsonb,
    evidence jsonb,
    decision_reference text,
    source_service text NOT NULL,
    source_device_id text,
    classification text NOT NULL CHECK (classification IN ('PUBLIC', 'STAFF_OPERATIONAL', 'PROTECTED_PERSONAL', 'LEGAL_EVIDENCE', 'FINANCIAL', 'SECURITY')),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX audit_events_correlation_idx ON audit.audit_events (correlation_id);
CREATE INDEX audit_events_target_idx ON audit.audit_events (target_type, target_id);

CREATE OR REPLACE FUNCTION audit.prevent_audit_event_update()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit events are append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_events_no_update
BEFORE UPDATE OR DELETE ON audit.audit_events
FOR EACH ROW EXECUTE FUNCTION audit.prevent_audit_event_update();

CREATE TABLE integrations.idempotency_records (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key text NOT NULL,
    operation text NOT NULL,
    request_hash text NOT NULL,
    response_status integer,
    response_body jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    CONSTRAINT idempotency_operation_key_unique UNIQUE (operation, idempotency_key)
);
