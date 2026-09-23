CREATE TABLE administration.administrative_units (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id uuid REFERENCES administration.administrative_units(id),
    unit_type text NOT NULL CHECK (unit_type IN (
        'COUNTRY', 'PROVINCE', 'CITY', 'TERRITORY', 'COMMUNE', 'SECTOR', 'CHIEFDOM',
        'GROUPEMENT', 'QUARTIER', 'LOCALITY', 'AVENUE', 'VILLAGE'
    )),
    code text NOT NULL,
    name text NOT NULL,
    alternative_names jsonb NOT NULL DEFAULT '[]'::jsonb,
    labels jsonb NOT NULL DEFAULT '{}'::jsonb,
    status text NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED', 'ARCHIVED')),
    valid_from date NOT NULL,
    valid_to date,
    geometry geometry(MultiPolygon, 4326),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT administrative_units_code_unique UNIQUE (unit_type, code),
    CONSTRAINT administrative_units_valid_dates CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT administrative_units_geometry_valid CHECK (geometry IS NULL OR ST_IsValid(geometry))
);

CREATE INDEX administrative_units_parent_idx ON administration.administrative_units(parent_id);
CREATE INDEX administrative_units_geometry_idx ON administration.administrative_units USING gist(geometry);

CREATE TABLE parcels.upi_scheme_versions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    scheme_code text NOT NULL,
    version_label text NOT NULL,
    description text NOT NULL DEFAULT '',
    pattern text NOT NULL,
    status text NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    effective_from date NOT NULL,
    effective_to date,
    created_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT upi_scheme_version_unique UNIQUE (scheme_code, version_label),
    CONSTRAINT upi_scheme_effective_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX one_active_upi_scheme_per_code
    ON parcels.upi_scheme_versions(scheme_code)
    WHERE status = 'ACTIVE';

CREATE TABLE parcels.parcels (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    status text NOT NULL CHECK (status IN (
        'DRAFT', 'UNDER_SURVEY', 'UNDER_REVIEW', 'ACTIVE', 'DISPUTED', 'RESTRICTED',
        'SUBDIVIDED', 'CONSOLIDATED', 'EXPROPRIATED', 'RETIRED', 'ARCHIVED'
    )),
    administrative_unit_id uuid NOT NULL REFERENCES administration.administrative_units(id),
    land_use text,
    zoning_classification text,
    tenure_classification text,
    declared_area_square_meters numeric(18, 2),
    data_quality_status text NOT NULL DEFAULT 'UNVERIFIED'
        CHECK (data_quality_status IN ('UNVERIFIED', 'VALIDATION_REQUIRED', 'VALIDATED', 'EXCEPTION')),
    source_system text NOT NULL DEFAULT 'edrc-land-gis',
    created_at timestamptz NOT NULL DEFAULT now(),
    effective_from timestamptz,
    retired_at timestamptz,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT parcel_retirement_date_required CHECK (
        status NOT IN ('RETIRED', 'ARCHIVED') OR retired_at IS NOT NULL
    )
);

CREATE INDEX parcels_status_idx ON parcels.parcels(status);
CREATE INDEX parcels_admin_unit_idx ON parcels.parcels(administrative_unit_id);

CREATE TABLE parcels.parcel_identifiers (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id uuid NOT NULL REFERENCES parcels.parcels(id),
    identifier_type text NOT NULL CHECK (identifier_type IN ('PROPOSED_UPI', 'REGISTRATION_REFERENCE', 'LEGACY_REFERENCE')),
    identifier_value text NOT NULL,
    upi_scheme_version_id uuid REFERENCES parcels.upi_scheme_versions(id),
    status text NOT NULL CHECK (status IN ('PROPOSED', 'ACTIVE', 'RETIRED', 'REJECTED')),
    assigned_at timestamptz NOT NULL DEFAULT now(),
    retired_at timestamptz,
    CONSTRAINT parcel_identifier_retirement_date_required CHECK (
        status != 'RETIRED' OR retired_at IS NOT NULL
    )
);

CREATE UNIQUE INDEX active_upi_values_unique
    ON parcels.parcel_identifiers(identifier_value)
    WHERE identifier_type = 'PROPOSED_UPI' AND status = 'ACTIVE';

CREATE UNIQUE INDEX parcel_one_active_upi
    ON parcels.parcel_identifiers(parcel_id)
    WHERE identifier_type = 'PROPOSED_UPI' AND status = 'ACTIVE';

CREATE TABLE parcels.parcel_state_transitions (
    from_status text NOT NULL,
    to_status text NOT NULL,
    requires_approval boolean NOT NULL DEFAULT true,
    PRIMARY KEY (from_status, to_status)
);

INSERT INTO parcels.parcel_state_transitions (from_status, to_status, requires_approval)
VALUES
    ('DRAFT', 'UNDER_SURVEY', false),
    ('UNDER_SURVEY', 'UNDER_REVIEW', true),
    ('UNDER_REVIEW', 'ACTIVE', true),
    ('ACTIVE', 'DISPUTED', true),
    ('ACTIVE', 'RESTRICTED', true),
    ('DISPUTED', 'ACTIVE', true),
    ('RESTRICTED', 'ACTIVE', true),
    ('ACTIVE', 'SUBDIVIDED', true),
    ('ACTIVE', 'CONSOLIDATED', true),
    ('ACTIVE', 'EXPROPRIATED', true),
    ('SUBDIVIDED', 'RETIRED', true),
    ('CONSOLIDATED', 'RETIRED', true),
    ('EXPROPRIATED', 'RETIRED', true),
    ('RETIRED', 'ARCHIVED', true);

CREATE TABLE parcels.parcel_geometry_versions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id uuid NOT NULL REFERENCES parcels.parcels(id),
    geometry geometry(MultiPolygon, 4326) NOT NULL,
    geometry_status text NOT NULL CHECK (geometry_status IN ('DRAFT', 'VALIDATED', 'APPROVED', 'REJECTED', 'SUPERSEDED')),
    calculated_area_square_meters numeric(18, 2) GENERATED ALWAYS AS (ST_Area(geometry::geography)) STORED,
    source text NOT NULL,
    effective_from timestamptz,
    effective_to timestamptz,
    approved_by uuid REFERENCES identity.users(id),
    approved_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT parcel_geometry_valid CHECK (ST_IsValid(geometry)),
    CONSTRAINT approved_geometry_has_approval CHECK (
        geometry_status != 'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL AND effective_from IS NOT NULL)
    )
);

CREATE UNIQUE INDEX parcel_one_current_approved_geometry
    ON parcels.parcel_geometry_versions(parcel_id)
    WHERE geometry_status = 'APPROVED' AND effective_to IS NULL;

CREATE INDEX parcel_geometry_versions_geometry_idx ON parcels.parcel_geometry_versions USING gist(geometry);
