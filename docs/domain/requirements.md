# Functional and Nonfunctional Requirements

## Functional Foundation

- Register users with secure password hashing and pending verification status.
- Maintain organizations, roles, permissions, and memberships.
- Record security and sensitive operational events in append-only audit tables.
- Provide legal-boundary disclaimer through public API and portal surfaces.
- Keep module and database schema boundaries explicit.
- Provide public parcel lookup only through a data-minimized projection that excludes protected owner and legal evidence information.
- Allow staff users to search parcels by administrative unit, direct parcel ID, and proposed UPI in the protected staff API.
- Allow staff users to create and list parcel draft geometry versions without publishing them as current approved cadastral geometry.
- Require evidence-linked maker-checker workflow before a draft parcel geometry version can become the current approved geometry.
- Enforce parcel lifecycle transitions through explicit allowed transitions instead of arbitrary status updates.
- Create durable workflow tasks when an allowed parcel transition requires approval, rather than changing registry state immediately.
- Allow staff users in the local slice to list open workflow tasks, claim a task, and approve or reject parcel status-transition tasks with a decision reason.
- Preserve pending parcel state until an approval decision is recorded; rejection must not mutate the parcel status.
- Enforce an initial maker-checker control for approval-required parcel status transitions by blocking the requesting user ID from approving their own task.
- Enforce workflow claim ownership by requiring the deciding user ID to match the user ID that claimed the task.
- Enforce workflow role scope by requiring the claiming user to have an active organization membership for the task's assigned role.
- Enforce parcel workflow jurisdiction scope by matching the claiming user's active membership province/jurisdiction to the parcel administrative unit or an ancestor unit.
- Allow staff users to attach controlled evidence references to workflow tasks.
- Require at least one linked evidence record before approving a workflow task.
- Create protected document metadata records with version, checksum, object-storage key, classification, retention category, malware-scan status, digital-signature status, and access policy.
- Store optional document custodian organization and custodian role scope for protected document custody.
- Append new document versions without overwriting previous version metadata.
- Enforce server-side document metadata read access for protected document endpoints; current policy allows the creator, active custodian-role members, or workflow-linked staff participants for workflow-scoped documents.
- Filter document owner-list responses so protected documents the actor cannot read are not returned.
- Restrict protected document version append in the current slice to the original document creator or active custodian-role members until approved custody-transfer rules exist.
- Record enriched but data-minimized audit events for protected document metadata creation, metadata reads, owner-list views, immutable version appends, and denied document access attempts, including actor user ID, direct request metadata, trusted-proxy-gated forwarded IP metadata, active registered device ID metadata, and safe evidence metadata.
- Allow staff users in the current slice to enroll and list registered devices used for audit traceability, and require evidence-linked security-officer maker-checker workflow before suspend, revoke, or expire mutations are applied.
- Provide a staff-console workflow operator path that routes parcel decisions to parcel workflow endpoints and registered-device lifecycle decisions to device lifecycle endpoints so approval screens do not accidentally apply the wrong target type.
- Show linked workflow evidence before approval decisions and warn officers when no evidence is linked, matching the backend rule that approvals require at least one evidence link.
- Regression-test high-risk device lifecycle changes so device mutation cannot occur before a claimed workflow task has linked evidence and a different checker approves it.
- Document registered-device lifecycle workflow endpoints in a checked-in OpenAPI contract slice, including lifecycle response shape, evidence endpoints, and decision failure modes.
- Validate `DOCUMENT` workflow evidence against existing document metadata records and permitted workflow evidence classifications.
- Record protected party records with party type, verification status, and data-confidence classification.
- Record parcel-linked ownership/use/customary/leasehold claims as protected interests without treating them as official registered title.
- Require evidence-linked human workflow review before marking an ownership/right claim operationally verified.
- Record parcel restrictions/cautions and block prohibited ownership-interest changes when an active restriction requires it.
- Accept parcel-information service requests with pending payment, officer review, operational report metadata, and auditable rejection/correction boundaries.
- Keep party and interest records out of public parcel projections.
- Avoid storing document binary content in transactional database columns.

## Nonfunctional Foundation

- All APIs are versioned under `/api/v1`.
- Responses use standardized problem details for validation and conflict errors.
- Correlation IDs are accepted or generated per request.
- Database migrations are the source of truth for schema changes.
- Secrets must remain outside source control.
- Local dependencies run through Docker Compose.
- CI runs backend tests, registered-device OpenAPI contract smoke checks, PostgreSQL/PostGIS-backed Flyway migration and API startup verification, frontend typecheck/build, npm moderate-or-higher dependency audit, and repository SBOM artifact generation on push and pull request.
