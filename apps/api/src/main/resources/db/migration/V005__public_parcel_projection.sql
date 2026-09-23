CREATE TABLE parcels.public_parcel_summaries (
    parcel_id uuid PRIMARY KEY REFERENCES parcels.parcels(id),
    status text NOT NULL,
    proposed_upi text NOT NULL,
    administrative_unit_id uuid NOT NULL REFERENCES administration.administrative_units(id),
    administrative_unit_name text NOT NULL,
    administrative_unit_type text NOT NULL,
    land_use text,
    tenure_classification text,
    published_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT public_parcel_summaries_upi_unique UNIQUE (proposed_upi)
);

CREATE INDEX public_parcel_summaries_upi_idx ON parcels.public_parcel_summaries(proposed_upi);
CREATE INDEX public_parcel_summaries_admin_unit_idx ON parcels.public_parcel_summaries(administrative_unit_id);
