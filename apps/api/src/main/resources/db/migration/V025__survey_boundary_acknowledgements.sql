CREATE TABLE surveys.boundary_acknowledgements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    survey_id UUID NOT NULL REFERENCES surveys.surveys(id),
    neighbor_name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACKNOWLEDGED', 'DECLINED', 'UNAVAILABLE')),
    note VARCHAR(1000),
    acknowledged_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    recorded_by_user_id UUID NOT NULL REFERENCES identity.users(id)
);

CREATE INDEX surveys_boundary_ack_survey_idx ON surveys.boundary_acknowledgements(survey_id, acknowledged_at);
