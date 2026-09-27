# Phase 19: Document governance review hardening

## Outcome

Phase 19 hardens the governed document export path so unsafe evidence cannot advance into a futile approval workflow. The platform now evaluates the latest immutable document version before creating an export review task and blocks exports when malware scanning is pending, malware scanning failed or the latest digital signature is invalid.

This remains a proposed land-information workflow system. It does not issue legally valid land titles, does not claim official Congolese registry authority and does not expose protected owner information publicly.

## Affected modules

- `governance`: document export decisions now inspect latest document-version safety status before opening approval work.
- `documents`: immutable version metadata is the source of malware-scan and digital-signature readiness.
- `workflow`: review tasks are opened only when blockers are limited to approval requirements.
- `staff-console`: staff can see a document governance summary and per-document export blockers before evidence is used for governed export.
- `audit`: existing governance and document audit events remain the record of export requests and document access.

## Data and API changes

No new database migration or external API is introduced in this phase.

The backend now treats these latest-version findings as hard export blockers:

- `MALWARE_SCAN_PENDING`
- `MALWARE_SCAN_FAILED`
- `DIGITAL_SIGNATURE_INVALID`
- `DOCUMENT_VERSION_MISSING`

Unsigned or unknown signatures remain visible metadata but are not hard blockers in this phase because historical scanned records may not have digital signatures.

## Security considerations

- Unsafe document versions are blocked before a workflow task is opened.
- Staff cannot override malware or invalid-signature blockers through frontend state.
- The decision uses repository-backed immutable version metadata, not user-entered export text.
- Approval still requires human review when protected or sensitive classifications require it.
- The UI labels blockers for operator awareness but the API remains the enforcement point.

## Privacy considerations

- The staff dashboard summarizes readiness counts without exposing personal ownership details.
- Public parcel projections remain separate from protected document evidence and export workflows.
- Export requests continue to require redaction planning when sensitive classifications demand it.

## Failure modes

- If malware scanning is still pending, export remains blocked until the status is updated.
- If malware scanning failed, export remains blocked and the document requires remediation or replacement by a new immutable version.
- If the latest signature is invalid, export remains blocked to prevent release of suspect evidence.
- If a document has no version metadata, staff should treat it as incomplete evidence before relying on it for legal or cadastral decisions.
- If staff-console rendering fails, backend governance still blocks unsafe export requests.

## Acceptance criteria

- Latest-version pending malware scan blocks document export.
- Latest-version failed malware scan blocks document export.
- Latest-version invalid digital signature blocks document export.
- Blocked unsafe exports do not open workflow approval tasks.
- Staff console shows a document governance summary for loaded owner documents.
- Staff console shows per-document export blockers.
- Data-governance guardrails enforce the new blockers, tests and documentation.
- Targeted backend tests and staff-console type checks pass.
