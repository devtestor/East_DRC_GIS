CREATE SCHEMA IF NOT EXISTS surveys;

CREATE TABLE surveys.surveys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id UUID NOT NULL REFERENCES parcels.parcels(id),
    assigned_to_user_id UUID NOT NULL REFERENCES identity.users(id),
    status VARCHAR(30) NOT NULL CHECK (status IN ('ASSIGNED', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED', 'REJECTED')),
    purpose VARCHAR(500) NOT NULL,
    submitted_at TIMESTAMPTZ,
    submitted_by_user_id UUID REFERENCES identity.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE surveys.observations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    survey_id UUID NOT NULL REFERENCES surveys.surveys(id),
    observation_type VARCHAR(40) NOT NULL CHECK (observation_type IN ('BOUNDARY_POINT', 'BOUNDARY_MARKER', 'WITNESS_NOTE', 'PHOTO_REFERENCE')),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    accuracy_meters NUMERIC(10,3),
    note VARCHAR(1000),
    captured_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    captured_by_user_id UUID NOT NULL REFERENCES identity.users(id)
);

CREATE INDEX surveys_parcel_idx ON surveys.surveys(parcel_id, created_at DESC);
CREATE INDEX surveys_assignee_idx ON surveys.surveys(assigned_to_user_id, status);
CREATE INDEX surveys_observations_survey_idx ON surveys.observations(survey_id, captured_at);
