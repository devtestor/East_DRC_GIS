CREATE TABLE documents.documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type text NOT NULL,
    owner_type text NOT NULL,
    owner_id uuid NOT NULL,
    title text NOT NULL,
    classification text NOT NULL CHECK (classification IN (
        'PUBLIC',
        'STAFF_OPERATIONAL',
        'PROTECTED_PERSONAL',
        'LEGAL_EVIDENCE',
        'FINANCIAL',
        'SECURITY'
    )),
    retention_category text NOT NULL,
    access_policy text NOT NULL,
    legal_hold boolean NOT NULL DEFAULT false,
    created_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    created_by text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    effective_at timestamptz,
    version bigint NOT NULL DEFAULT 0
);

CREATE TABLE documents.document_versions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id uuid NOT NULL REFERENCES documents.documents(id),
    version_number integer NOT NULL,
    object_storage_key text NOT NULL,
    original_filename text NOT NULL,
    media_type text NOT NULL,
    size_bytes bigint NOT NULL CHECK (size_bytes >= 0),
    checksum_sha256 text NOT NULL CHECK (checksum_sha256 ~ '^[A-Fa-f0-9]{64}$'),
    malware_scan_status text NOT NULL CHECK (malware_scan_status IN (
        'PENDING',
        'PASSED',
        'FAILED',
        'NOT_REQUIRED'
    )),
    digital_signature_status text NOT NULL CHECK (digital_signature_status IN (
        'UNSIGNED',
        'VALID',
        'INVALID',
        'UNKNOWN'
    )),
    uploaded_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    uploaded_by text NOT NULL,
    uploaded_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT document_version_unique UNIQUE (document_id, version_number),
    CONSTRAINT document_object_key_unique UNIQUE (object_storage_key),
    CONSTRAINT document_checksum_unique UNIQUE (checksum_sha256)
);

CREATE INDEX documents_owner_idx ON documents.documents(owner_type, owner_id);
CREATE INDEX documents_classification_idx ON documents.documents(classification);
CREATE INDEX document_versions_document_idx ON documents.document_versions(document_id);
