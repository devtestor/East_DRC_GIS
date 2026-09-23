# Phase 5 Implementation Notes

## First vertical slice: controlled field survey

- Survey jobs can be assigned to a field officer and listed by the assigned officer.
- Field observations are append-only while a survey is editable.
- Submission requires at least one observation and a proposed WKT geometry.
- The submitted geometry is stored as a `DRAFT` parcel geometry version with a `FIELD_SURVEY` source reference.
- Current approved parcel geometry is never replaced by survey submission.
- Existing cadastral geometry review and maker-checker approval remain the publication gate.
- Offline observations carry a client-generated UUID; V024 enforces replay idempotency so reconnecting a field device cannot duplicate an observation.
- `apps/field-mobile` provides the Android-first Flutter foundation with encrypted SQLCipher storage, secure key storage, and a serialized sync queue.

## API endpoints

- `POST /api/v1/surveys`
- `GET /api/v1/surveys`
- `POST /api/v1/surveys/{surveyId}/observations`
- `POST /api/v1/surveys/{surveyId}/submit`

The mobile app currently uses `http://10.0.2.2:8080` for the Android emulator and requires device enrollment before pilot use.

## Remaining Phase 5 work

- Flutter package validation on a machine with the Flutter SDK, plus full resumable synchronization and conflict resolution UI.
- GNSS/device attestation and field-photo object storage.
- Neighbor acknowledgement and boundary-marker workflows.
- Split/consolidation operations driven by approved survey geometry.
- Spatial topology and jurisdiction validation expansion.
