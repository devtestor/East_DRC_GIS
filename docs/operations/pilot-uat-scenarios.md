# Pilot UAT Scenarios

## Citizen Portal

- Search public parcel information and confirm protected owner data is not exposed.
- Submit a parcel-information request with fictional evidence.
- Pay a sandbox invoice and confirm the application remains pending human review.
- Receive a notification without sensitive ownership information.
- Submit a correction or appeal request.

## Staff Console

- Create an administrative unit with configurable hierarchy.
- Create a draft parcel with a proposed UPI.
- Confirm duplicate active UPI values are rejected.
- Move a parcel through explicit state transitions.
- Verify the pilot readiness panel requires staff authentication.

## Cadastral Operations

- Submit draft geometry and confirm it is not current until approved.
- Reject invalid geometry.
- Request cadastral approval and confirm maker-checker separation.
- Confirm prior geometry remains preserved after approval.

## Parties, Rights and Restrictions

- Create a fictional party and ownership claim.
- Add competing claims and confirm conflict signals are investigation leads only.
- Apply a parcel restriction and confirm prohibited transactions are blocked.
- Request release of a restriction and confirm human approval is required.

## Survey and Field Operations

- Enroll a field device.
- Create an assigned survey job.
- Capture offline-style observations with device and capture metadata.
- Record neighbor acknowledgement.
- Submit a proposed geometry for cadastral review.
- Revoke a field device through an approval task.

## Documents and Evidence

- Upload document metadata with checksum and classification.
- Add a new version without replacing historical evidence.
- Confirm access policy prevents unauthorized document access.
- Confirm malware-scan status is visible in metadata.

## Payments and Reconciliation

- Initiate sandbox payment.
- Process duplicate callback and confirm idempotent behavior.
- Reconcile unmatched payment records.
- Confirm payment confirmation does not automatically perform a legal registry change.

## Security and Audit

- Confirm anonymous users cannot access protected APIs.
- Confirm role-scoped users cannot approve unrelated high-risk tasks.
- Confirm audit events include actor, organization context where available, target and correlation ID.
- Confirm sensitive tokens, passwords and full identity documents are not logged.
