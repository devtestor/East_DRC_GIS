INSERT INTO identity.roles (code, name, description)
VALUES (
    'SECURITY_OFFICER',
    'Security Officer',
    'Reviews high-risk security administration actions such as registered-device lifecycle changes'
)
ON CONFLICT (code) DO NOTHING;
