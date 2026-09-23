CREATE TABLE parties.parties (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    party_type text NOT NULL CHECK (party_type IN (
        'INDIVIDUAL',
        'ORGANIZATION',
        'GOVERNMENT_INSTITUTION',
        'ESTATE',
        'COOPERATIVE'
    )),
    display_name text NOT NULL,
    data_confidence text NOT NULL CHECK (data_confidence IN (
        'UNVERIFIED',
        'SELF_DECLARED',
        'DOCUMENT_SUPPORTED',
        'INSTITUTION_VERIFIED'
    )),
    verification_status text NOT NULL CHECK (verification_status IN (
        'PENDING',
        'VERIFIED',
        'REJECTED',
        'SUSPENDED'
    )),
    created_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    created_by text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0
);

CREATE INDEX parties_type_idx ON parties.parties(party_type);
CREATE INDEX parties_verification_status_idx ON parties.parties(verification_status);

CREATE TABLE rights.ownership_interests (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id uuid NOT NULL REFERENCES parcels.parcels(id),
    party_id uuid NOT NULL REFERENCES parties.parties(id),
    interest_type text NOT NULL CHECK (interest_type IN (
        'OWNERSHIP_CLAIM',
        'OCCUPANCY_CLAIM',
        'USE_RIGHT_CLAIM',
        'CUSTOMARY_CLAIM',
        'LEASEHOLD_CLAIM'
    )),
    interest_status text NOT NULL CHECK (interest_status IN (
        'CLAIMED',
        'UNDER_REVIEW',
        'VERIFIED',
        'REJECTED',
        'RETIRED'
    )),
    share_percent numeric(5, 2) CHECK (share_percent IS NULL OR (share_percent > 0 AND share_percent <= 100)),
    data_confidence text NOT NULL CHECK (data_confidence IN (
        'UNVERIFIED',
        'SELF_DECLARED',
        'DOCUMENT_SUPPORTED',
        'INSTITUTION_VERIFIED'
    )),
    source text NOT NULL,
    effective_from timestamptz,
    effective_to timestamptz,
    created_by_user_id uuid NOT NULL REFERENCES identity.users(id),
    created_by text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT ownership_interest_effective_dates CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
);

CREATE INDEX ownership_interests_parcel_idx ON rights.ownership_interests(parcel_id);
CREATE INDEX ownership_interests_party_idx ON rights.ownership_interests(party_id);
CREATE INDEX ownership_interests_status_idx ON rights.ownership_interests(interest_status);
