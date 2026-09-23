CREATE TABLE payments.provider_callbacks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(80) NOT NULL,
    provider_event_id VARCHAR(180) NOT NULL,
    invoice_id UUID REFERENCES payments.invoices(id),
    event_status VARCHAR(30) NOT NULL CHECK (event_status IN ('RECEIVED', 'APPLIED', 'IGNORED', 'FAILED')),
    payload_checksum VARCHAR(64) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    failure_reason VARCHAR(500),
    UNIQUE (provider, provider_event_id)
);

CREATE TABLE payments.reconciliation_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider VARCHAR(80) NOT NULL,
    provider_reference VARCHAR(180) NOT NULL,
    invoice_id UUID REFERENCES payments.invoices(id),
    expected_amount NUMERIC(19,4),
    received_amount NUMERIC(19,4),
    currency VARCHAR(3),
    status VARCHAR(30) NOT NULL CHECK (status IN ('MATCHED', 'UNMATCHED', 'AMOUNT_MISMATCH', 'CURRENCY_MISMATCH')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (provider, provider_reference)
);
