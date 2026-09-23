CREATE TABLE disputes.parcel_restrictions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id uuid NOT NULL REFERENCES parcels.parcels(id),
    restriction_type text NOT NULL CHECK (
        restriction_type IN (
            'BOUNDARY_DISPUTE',
            'OWNERSHIP_DISPUTE',
            'COURT_CAUTION',
            'FRAUD_ALLEGATION',
            'ADMINISTRATIVE_FREEZE',
            'EXPROPRIATION_NOTICE',
            'OTHER'
        )
    ),
    status text NOT NULL CHECK (status IN ('ACTIVE', 'RELEASED', 'EXPIRED')),
    source text NOT NULL,
    authority_reference text,
    summary text NOT NULL,
    blocks_ownership_changes boolean NOT NULL DEFAULT true,
    effective_from timestamptz NOT NULL DEFAULT now(),
    effective_to timestamptz,
    created_by_user_id uuid REFERENCES identity.users(id),
    created_by text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    released_by_user_id uuid REFERENCES identity.users(id),
    released_by text,
    released_at timestamptz,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT parcel_restriction_effective_window CHECK (effective_to IS NULL OR effective_to > effective_from)
);

CREATE INDEX parcel_restrictions_parcel_idx ON disputes.parcel_restrictions(parcel_id);
CREATE INDEX parcel_restrictions_active_idx
    ON disputes.parcel_restrictions(parcel_id)
    WHERE status = 'ACTIVE';

CREATE OR REPLACE FUNCTION disputes.prevent_restricted_ownership_interest_change()
RETURNS trigger AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM disputes.parcel_restrictions restriction
        WHERE restriction.parcel_id = NEW.parcel_id
          AND restriction.status = 'ACTIVE'
          AND restriction.blocks_ownership_changes = true
          AND restriction.effective_from <= now()
          AND (restriction.effective_to IS NULL OR restriction.effective_to > now())
    ) THEN
        RAISE EXCEPTION 'Parcel has an active restriction blocking ownership-interest changes'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER ownership_interest_restriction_guard
BEFORE INSERT OR UPDATE OF party_id, interest_type, interest_status, share_percent, effective_from, effective_to
ON rights.ownership_interests
FOR EACH ROW EXECUTE FUNCTION disputes.prevent_restricted_ownership_interest_change();
