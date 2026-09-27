CREATE TABLE governance.export_packages (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    export_request_id uuid NOT NULL REFERENCES governance.export_requests(id),
    document_id uuid NOT NULL REFERENCES documents.documents(id),
    document_version_id uuid NOT NULL REFERENCES documents.document_versions(id),
    object_storage_key text NOT NULL,
    manifest_sha256 text NOT NULL CHECK (manifest_sha256 ~ '^[a-f0-9]{64}$'),
    package_sha256 text NOT NULL CHECK (package_sha256 ~ '^[a-f0-9]{64}$'),
    package_size_bytes bigint NOT NULL CHECK (package_size_bytes > 0),
    token_sha256 text NOT NULL CHECK (token_sha256 ~ '^[a-f0-9]{64}$'),
    expires_at timestamptz NOT NULL,
    generated_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    generated_by text NOT NULL,
    generated_at timestamptz NOT NULL DEFAULT now(),
    downloaded_at timestamptz,
    last_downloaded_by_user_id uuid REFERENCES identity.users(id),
    last_downloaded_by text,
    download_count integer NOT NULL DEFAULT 0 CHECK (download_count >= 0),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT export_packages_expiry_after_generation CHECK (expires_at > generated_at)
);

CREATE UNIQUE INDEX export_packages_request_unique_idx ON governance.export_packages(export_request_id);
CREATE INDEX export_packages_document_idx ON governance.export_packages(document_id, generated_at);
CREATE INDEX export_packages_expiry_idx ON governance.export_packages(expires_at);
