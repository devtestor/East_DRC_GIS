# Initial Data Model

## Phase 1 Schemas

- `identity`: users, organizations, roles, permissions, memberships.
- `audit`: append-only audit events.
- `integrations`: idempotency records.

The migration also creates placeholder schemas for administration, parcels, cadastre, parties, rights, transactions, workflow, documents, payments, and disputes so module boundaries are explicit from the beginning.

## Core Invariants Started in Phase 1

- User email values are unique.
- Organization and account statuses are constrained.
- Audit events cannot be updated or deleted through normal database operations.
- Idempotency keys are unique per operation.
- Database migrations, not JPA auto-DDL, own schema changes.

## Phase 2 Expansion

Phase 2 should add administrative units, parcel identifiers, UPI scheme versions, parcel lifecycle states, parcel geometry versions, and database constraints for active UPI uniqueness and single current approved geometry.

Initial Phase 2 migrations now add these structures. Geometry publication remains controlled through the geometry version table and must not be treated as current merely because a draft parcel exists. The current API can create and list `DRAFT` geometry versions for a parcel. A `PARCEL_GEOMETRY_APPROVAL` workflow can approve one draft as the current geometry, superseding any prior current approved geometry in the same transaction while preserving all prior versions.

Public parcel lookup uses `parcels.public_parcel_summaries`, a data-minimized projection maintained when local draft parcels are created. This projection deliberately excludes party, owner, document, dispute, evidence, and payment data.

`workflow.tasks` now stores the first durable human-task scaffold for approval-required transitions. It includes status, priority, assignment role, requester user ID plus display label, claimed user ID plus display label, claimed timestamp, decision reason, decision timestamp, and decision user ID plus display label for the local slice. `workflow.task_evidence_links` stores controlled evidence references attached to tasks. `DOCUMENT` evidence must reference an existing `documents.documents` record and cannot use public/security-only classifications as workflow evidence. The current maker-checker invariant blocks the requester user from approving the same task, decisions require the task to be claimed by the deciding user, and approvals require at least one evidence link. It is intentionally minimal and will need SLA, escalation, and appeal metadata before production workflow use.

`documents.documents` and `documents.document_versions` now provide the first document metadata slice. They store document ownership, classification, retention, access policy, optional custodian organization and custodian role scope, legal hold, object-storage key, checksum, media metadata, malware-scan status, and digital-signature status. Binary file contents must remain in encrypted object storage and are not stored in transactional database columns. Document updates append immutable version rows; they do not overwrite earlier evidence metadata.

`identity.registered_devices` stores the first formal device-enrollment slice for audit traceability. A device ID is recorded on audit events only when it matches the safe format rule and has an active, non-expired, non-revoked enrollment record. The current API supports staff-only enrollment and listing. Suspension, revocation, and expiry no longer mutate the record directly; they create `workflow.tasks` entries with workflow type `REGISTERED_DEVICE_LIFECYCLE`, target type `registered-device`, requested action `SUSPEND`, `REVOKE`, or `EXPIRE`, and assigned role `SECURITY_OFFICER`. The approved decision path applies the lifecycle mutation atomically after task claim, evidence, and maker-checker controls. It does not yet provide remote wipe or mobile-device synchronization workflows.

## Phase 3 Expansion

`parties.parties` stores protected party records with party type, display label, verification status, and data-confidence classification. `rights.ownership_interests` stores parcel-linked claims and interests with interest type, status, optional share percentage, source, confidence, and creator metadata. These records are protected operational/legal-evidence data and are not included in public parcel projections. The current status defaults to `CLAIMED`; evidence-linked review can move a record to `UNDER_REVIEW`, `VERIFIED`, or `REJECTED`. `VERIFIED` is an operational review status only, not official title registration. Ownership transfer and official registration workflows are not implemented in this slice.

`disputes.parcel_restrictions` stores parcel cautions, disputes, administrative freezes, and similar protected controls. Active restrictions can block ownership-interest insert/update operations when `blocks_ownership_changes` is true. This invariant is enforced in application services and by the `ownership_interest_restriction_guard` database trigger.

The ownership-interest conflict projection reports multiple active ownership claims and ownership-share totals above 100 percent. These are investigation signals only; no conflict projection changes a right or makes a legal determination.

`disputes.case_documents` links an accessible protected document to a dispute case with a controlled relationship such as `COURT_ORDER` or `SURVEY_EVIDENCE`. The link is append-only, preserves the document's own version/checksum history, and records the linking actor and time.

`disputes.hearings` records scheduled/held/cancelled hearing events. `disputes.appeals` records appeal grounds and status separately from the original case. Operational decisions and reopening are workflow-gated; an approved decision stores an outcome summary and actor metadata but is not represented as a legally binding court judgment.

`transactions.applications` and `payments.invoices` provide the first Phase 4 service workflow. The current application type is `PARCEL_INFORMATION_REQUEST`; invoice state is separate from the application state, and sandbox payment confirmation is idempotent. Approval creates only protected document metadata for an operational report.
