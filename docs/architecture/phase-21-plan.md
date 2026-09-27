# Phase 21: Document quarantine and release controls

## Outcome

Phase 21 adds an explicit quarantine lifecycle for document versions. A document version can be quarantined without deleting or replacing evidence, and a quarantined latest version blocks governed exports until an authorized human releases it after a clean safety review.

This remains a proposed workflow system. Quarantine/release decisions are operational evidence-safety controls and are not legal findings about parcel ownership, rights or title validity.

## Affected modules

- `documents`: adds safety lifecycle state, quarantine/release endpoints, release guardrails and audit evidence.
- `governance`: blocks governed document export when the latest version is quarantined.
- `staff-console`: adds a quarantine/release form for the selected document version.
- `audit`: records quarantine, release, denied and blocked release events.

## Database changes

Migration `V033__document_version_quarantine.sql` extends `documents.document_versions` with:

- `safety_status`: constrained to `AVAILABLE` or `QUARANTINED`.
- `safety_reason`: human review reason.
- `safety_reviewed_by_user_id`, `safety_reviewed_by`, `safety_reviewed_at`: reviewer traceability.

The migration also adds a completeness check so a safety review timestamp, user id and actor label are recorded together.

## API changes

- `POST /api/v1/documents/{documentId}/versions/{versionId}/quarantine`
- `POST /api/v1/documents/{documentId}/versions/{versionId}/release`

Request body:

```json
{
  "reason": "Sandbox malware scan failed; evidence quarantined"
}
```

Release is rejected with a conflict problem when the version still has `FAILED` malware scan status or `INVALID` digital-signature status.

## Security and privacy considerations

- Staff authentication is required by the existing API security policy.
- The document service also enforces creator/custodian authority before quarantine or release.
- Denied quarantine/release attempts are security audit events.
- Reasons must not include passwords, access tokens, full identity document content, owner personal details or unmasked financial information.

## Failure modes

- Missing document returns the document-not-found problem.
- Missing version returns the document-version-not-found problem.
- Unauthorized actor receives document-access-denied and a security audit record is created.
- Unsafe release receives document-version-release-blocked and the version remains quarantined.
- Export requests against quarantined latest versions include `DOCUMENT_VERSION_QUARANTINED`.

## Acceptance criteria

- Authorized staff can quarantine an existing document version with a reason.
- Authorized staff can release a quarantined version only when scan/signature statuses are safe.
- Quarantined latest document versions block governed export.
- The staff console exposes quarantine/release controls without modifying object key, checksum or prior versions.
- Service, MVC and governance tests cover the new lifecycle.
