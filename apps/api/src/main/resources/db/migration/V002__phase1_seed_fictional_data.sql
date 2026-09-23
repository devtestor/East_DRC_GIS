INSERT INTO identity.roles (code, name, description)
VALUES
    ('PLATFORM_ADMIN', 'Platform administrator', 'Technical administration without automatic legal registry authority'),
    ('CADASTRAL_OFFICER', 'Cadastral officer', 'Reviews cadastral data and survey evidence'),
    ('LAND_TITLE_OFFICER', 'Land-title officer', 'Reviews protected registry records when authorized'),
    ('CITIZEN', 'Citizen', 'Citizen portal user')
ON CONFLICT (code) DO NOTHING;

INSERT INTO identity.permissions (code, description)
VALUES
    ('identity.user.read', 'Read user account records within authorized scope'),
    ('audit.event.read', 'Read audit events within authorized scope'),
    ('parcel.public.read', 'Read public parcel projection'),
    ('registry.legal-change.request', 'Request legal registry-affecting change'),
    ('registry.legal-change.approve', 'Approve legal registry-affecting change subject to maker-checker controls')
ON CONFLICT (code) DO NOTHING;
