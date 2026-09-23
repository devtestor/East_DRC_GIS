CREATE TABLE identity.registered_devices (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id text NOT NULL UNIQUE,
    assigned_user_id uuid REFERENCES identity.users(id),
    organization_id uuid REFERENCES identity.organizations(id),
    device_type text NOT NULL CHECK (device_type IN (
        'FIELD_MOBILE',
        'STAFF_WORKSTATION',
        'SERVICE_ACCOUNT',
        'OTHER'
    )),
    status text NOT NULL CHECK (status IN (
        'PENDING_ENROLLMENT',
        'ACTIVE',
        'SUSPENDED',
        'REVOKED',
        'EXPIRED'
    )),
    enrolled_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz,
    revoked_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT registered_device_id_format
        CHECK (device_id ~ '^[A-Za-z0-9][A-Za-z0-9._:-]{2,79}$'),
    CONSTRAINT registered_device_revocation_consistent
        CHECK ((status = 'REVOKED' AND revoked_at IS NOT NULL) OR (status <> 'REVOKED'))
);

CREATE INDEX registered_devices_assigned_user_idx ON identity.registered_devices(assigned_user_id);
CREATE INDEX registered_devices_organization_idx ON identity.registered_devices(organization_id);
CREATE INDEX registered_devices_status_idx ON identity.registered_devices(status);
