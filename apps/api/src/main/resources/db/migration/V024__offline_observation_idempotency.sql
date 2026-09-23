ALTER TABLE surveys.observations
    ADD COLUMN client_observation_id UUID;

CREATE UNIQUE INDEX surveys_observations_client_id_idx
    ON surveys.observations(survey_id, client_observation_id)
    WHERE client_observation_id IS NOT NULL;
