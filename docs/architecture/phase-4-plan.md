# Phase 4 Implementation Notes

## First vertical slice: parcel information request

- Authenticated users can submit a parcel-information request against a parcel UUID.
- Submission creates a `PAYMENT_PENDING` application and a provider-independent `SANDBOX` invoice.
- Sandbox payment confirmation is idempotent and moves the service request to `UNDER_REVIEW`; it does not change parcel, rights, geometry, or official registry state.
- Staff can open an evidence-backed `PARCEL_INFORMATION_REQUEST_REVIEW` task scoped to the parcel jurisdiction.
- Maker-checker approval produces a protected document metadata record for the information report and marks the application `APPROVED`; rejection preserves the decision reason.
- The requesting applicant can read the approved report metadata through the document endpoint only when the application is approved and the document is linked to that application; unrelated users remain denied.
- An authenticated in-application notification inbox records submission, payment confirmation, approval, and rejection notices with idempotent deduplication and recipient-only access.
- The generated report is an operational service output and not an official land title or government certificate.

## API endpoints

- `POST /api/v1/applications/parcel-information-requests`
- `GET /api/v1/applications/{applicationId}`
- `POST /api/v1/applications/{applicationId}/payments/sandbox-confirmation`
- `POST /api/v1/applications/{applicationId}/review-requests`
- `POST /api/v1/applications/tasks/{taskId}/decisions`
- `GET /api/v1/documents/{documentId}` (applicant-scoped after approval)
- `GET /api/v1/notifications`
- `POST /api/v1/notifications/{notificationId}/read`

## Phase 4 completion boundary

- The first controlled citizen service transaction is complete through submission, sandbox payment,
  human review, approval/rejection, correction request, notification, application history, and
  applicant-scoped sandbox document download.
- Real payment, SMS/email, encrypted object storage, malware scanning, additional services, and
  appeals remain integration/hardening work for later phases. No external provider or official
  government registry is represented by the sandbox adapters.
