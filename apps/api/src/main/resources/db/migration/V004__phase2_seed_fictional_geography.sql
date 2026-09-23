INSERT INTO parcels.upi_scheme_versions (
    scheme_code,
    version_label,
    description,
    pattern,
    status,
    effective_from
)
VALUES (
    'PROPOSED-EDRC',
    'v0.1-fictional',
    'Fictional proposed UPI scheme for development and testing only; not an official standard.',
    '[A-Z0-9.-]{6,64}',
    'ACTIVE',
    DATE '2026-01-01'
)
ON CONFLICT (scheme_code, version_label) DO NOTHING;

INSERT INTO administration.administrative_units (
    unit_type,
    code,
    name,
    labels,
    status,
    valid_from
)
VALUES (
    'PROVINCE',
    'NK-FICTIONAL',
    'Nord-Kivu Fictional Pilot Province',
    '{"fr": "Province pilote fictive du Nord-Kivu", "sw": "Jimbo la majaribio la kubuni la Kivu Kaskazini"}'::jsonb,
    'ACTIVE',
    DATE '2026-01-01'
)
ON CONFLICT (unit_type, code) DO NOTHING;
