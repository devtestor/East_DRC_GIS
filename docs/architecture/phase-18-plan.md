# Phase 18: Document evidence intake staff workflow

## Outcome

Phase 18 adds the first staff-facing document and evidence intake workflow. It remains a metadata-first sandbox workflow: binary content is referenced through object-storage keys and downloaded only through the protected sandbox content endpoint. The platform still does not issue official land titles or expose protected owner information publicly.

## Affected modules

- `documents`: staff console can create document metadata with the first immutable version, load document metadata, list documents by governed owner, append immutable versions and download sandbox content.
- `workflow`: staff console can attach a document UUID as governed task evidence through `referenceId`.
- `governance`: export requests can now start from documents created through the staff intake panel.
- `audit`: backend document create, read, version append and content download audit events remain the control source.

## Data and API use

No new migration is introduced in this phase. The UI consumes existing endpoints:

- `POST /api/v1/documents`
- `GET /api/v1/documents/{documentId}`
- `GET /api/v1/documents?ownerType={ownerType}&ownerId={ownerId}`
- `POST /api/v1/documents/{documentId}/versions`
- `GET /api/v1/documents/{documentId}/content`
- `POST /api/v1/workflow/tasks/{taskId}/evidence`

Document fields surfaced in the UI include classification, retention category, access policy, custodian organization, custodian role, legal hold, object-storage key, filename, media type, size, SHA-256 checksum, malware-scan status and digital-signature status.

## Security considerations

- The staff console sends requests through authenticated API calls with correlation IDs.
- Protected document reads and version appends are still authorized by the backend; the UI does not bypass access policy.
- High-risk documents can be classified as protected personal, legal evidence, financial or security data.
- Document evidence links now include `referenceId`, allowing workflow approval checks to validate the referenced document.
- Sandbox downloads go through the API and do not reveal object-storage credentials.

## Privacy considerations

- The UI explicitly models classification, retention, access policy and legal hold during intake.
- Public parcel projections remain separate from protected document metadata.
- SMS/email notification guidance from the export workflow still applies: sensitive owner, legal, financial and identity information must not be placed in unsecured messages.

## Failure modes

- If object storage is unavailable, document metadata and workflow evidence remain intact; sandbox content download may fail independently.
- If malware scan is pending or failed, the status remains visible and export governance can block unsafe release.
- If the acting user lacks custodian access, the backend denies read or version-append operations and records security audit events.
- If a workflow task requires evidence, approval remains blocked until a valid evidence link exists.

## Acceptance criteria

- Staff can create a document metadata record with version 1.
- Staff can list documents by governed owner and reload a selected document.
- Staff can append a new immutable version without replacing prior metadata.
- Staff can see classification, access policy, legal hold, checksum, malware-scan and signature status.
- Staff can attach a document UUID as workflow task evidence.
- Staff can request a sandbox document download through the protected API.
- Type checking and data-governance guardrails pass.
