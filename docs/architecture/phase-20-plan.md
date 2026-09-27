# Phase 20: Document scan and signature lifecycle review

## Outcome

Phase 20 adds a controlled lifecycle operation for document-version safety metadata. Authorized staff can update the malware-scan and digital-signature status of an existing immutable document version after a sandbox scanner or human document-security review completes.

The operation does not replace the stored object key, checksum, filename, upload actor or previous versions. It only updates the latest security-readiness metadata needed by governed export controls. The platform remains a proposed land-information workflow system and does not issue legally valid land titles.

## Affected modules

- `documents`: adds the staff-only document-version safety status endpoint and audited service operation.
- `governance`: Phase 19 export blockers automatically consume the updated latest-version status.
- `staff-console`: adds a review form for updating selected document-version malware/signature status with a reason.
- `audit`: records successful and denied safety-status updates.

## Data and API changes

No database migration is required. Existing `documents.document_versions` status columns are updated in place for scanner/reviewer lifecycle metadata.

New endpoint:

- `POST /api/v1/documents/{documentId}/versions/{versionId}/safety-status`

Request body:

```json
{
  "malwareScanStatus": "PASSED",
  "digitalSignatureStatus": "VALID",
  "reason": "Sandbox malware scan passed and signature verified"
}
```

## Security considerations

- The route remains staff-only through server-side security configuration.
- The service also requires the same creator/custodian authority used for appending document versions.
- Unauthorized attempts create a `document.version-safety-update-denied` audit event.
- Successful updates create a `document.version-safety-status-updated` audit event with previous and new statuses.
- Frontend state is advisory only; backend policy and repository state decide authorization.

## Privacy considerations

- The request reason must be operational and must not contain full identity documents, secrets, payment data or unnecessary owner details.
- The staff console shows scan/signature readiness for protected records but does not expose protected owner information publicly.

## Failure modes

- If the document does not exist, the API returns a document-not-found problem.
- If the version is not attached to the document, the API returns a document-version-not-found problem.
- If the actor lacks creator/custodian authority, the update is denied and audited.
- If the scanner reports `FAILED` or the signature remains `INVALID`, governed exports stay blocked by Phase 19 controls.

## Acceptance criteria

- Authorized staff can update malware-scan and digital-signature status for an existing document version.
- The update does not change object key, checksum, filename, uploaded actor or version lineage.
- Denied updates are audited and rejected.
- Staff console can submit the update with a reason and refresh the selected document.
- Governance blockers continue to reflect the latest version status.
- Targeted backend tests, staff-console typecheck/build and data-governance guardrails pass.
