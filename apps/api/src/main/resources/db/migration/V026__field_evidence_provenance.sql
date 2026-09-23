ALTER TABLE surveys.observations
    ADD COLUMN device_id TEXT,
    ADD COLUMN capture_source TEXT NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN gnss_fix_quality TEXT,
    ADD COLUMN photo_object_key TEXT,
    ADD COLUMN signature_status TEXT NOT NULL DEFAULT 'NOT_CAPTURED';

ALTER TABLE surveys.observations
    ADD CONSTRAINT survey_observation_capture_source_check
        CHECK (capture_source IN ('GNSS', 'MANUAL', 'IMPORTED')),
    ADD CONSTRAINT survey_observation_signature_status_check
        CHECK (signature_status IN ('NOT_CAPTURED', 'CAPTURED', 'VERIFIED'));

CREATE INDEX surveys_observations_device_idx ON surveys.observations(device_id, captured_at);
