ALTER TABLE documents.documents
    ADD COLUMN custodian_organization_id uuid REFERENCES identity.organizations(id),
    ADD COLUMN custodian_role_code text;

ALTER TABLE documents.documents
    ADD CONSTRAINT document_custody_role_requires_org
    CHECK (
        (custodian_organization_id IS NULL AND custodian_role_code IS NULL)
        OR (custodian_organization_id IS NOT NULL AND custodian_role_code IS NOT NULL AND length(trim(custodian_role_code)) > 0)
    );

CREATE INDEX documents_custodian_org_idx ON documents.documents(custodian_organization_id);
