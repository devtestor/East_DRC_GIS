CREATE TABLE transactions.applications (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id uuid NOT NULL REFERENCES parcels.parcels(id),
    application_type text NOT NULL CHECK (application_type = 'PARCEL_INFORMATION_REQUEST'),
    status text NOT NULL CHECK (status IN ('SUBMITTED', 'PAYMENT_PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CORRECTION_REQUESTED', 'CLOSED')),
    applicant_user_id uuid NOT NULL REFERENCES identity.users(id),
    applicant_actor text NOT NULL,
    purpose text NOT NULL,
    decision_reason text,
    generated_document_id uuid REFERENCES documents.documents(id),
    submitted_at timestamptz NOT NULL DEFAULT now(),
    decided_at timestamptz,
    decided_by_user_id uuid REFERENCES identity.users(id),
    decided_by text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE TABLE payments.invoices (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id uuid NOT NULL UNIQUE REFERENCES transactions.applications(id),
    invoice_number text NOT NULL UNIQUE,
    amount numeric(12, 2) NOT NULL CHECK (amount >= 0),
    currency char(3) NOT NULL,
    status text NOT NULL CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REVERSED', 'REFUNDED')),
    provider text NOT NULL,
    payment_reference text,
    paid_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE INDEX transaction_applications_parcel_idx ON transactions.applications(parcel_id);
CREATE INDEX transaction_applications_applicant_idx ON transactions.applications(applicant_user_id);
CREATE INDEX transaction_applications_status_idx ON transactions.applications(status);
CREATE INDEX payment_invoices_status_idx ON payments.invoices(status);

INSERT INTO payments.invoices (id, application_id, invoice_number, amount, currency, status, provider)
SELECT gen_random_uuid(), gen_random_uuid(), 'SEED-NOT-USED', 0, 'USD', 'PENDING', 'SANDBOX'
WHERE false;
