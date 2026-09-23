# Phase 2 Implementation Notes

## Outcome

Start the administrative geography and parcel registry foundation without hard-coding the Eastern DRC hierarchy.

## Implemented Slice

- Configurable administrative units with parent-child references, types, dates, status, optional geometry, and multilingual labels at the database layer.
- Proposed UPI scheme version table with a fictional active development scheme.
- Parcel records with explicit lifecycle states.
- Parcel identifiers with database-enforced active proposed UPI uniqueness.
- Parcel geometry version table with PostGIS validity checks and one current approved geometry invariant.
- Explicit parcel state transition table.
- Staff API endpoints to create administrative units and draft parcels.
- Staff parcel lookup supports administrative-unit lists, direct parcel ID reads, and proposed-UPI search with active UPI values included in staff responses.
- Audit events for administrative-unit creation and draft parcel creation.
- Database-backed HTTP Basic authentication for the current local slice.
- Fictional local staff seed account guarded by `DEV_SEED_ENABLED`.
- Staff-console form workflow for creating administrative units and draft parcels.
- Staff console quick parcel lookup by proposed UPI reuses the returned parcel ID for transitions and draft geometry capture.
- Public parcel search projection by proposed UPI that excludes party, owner, document, financial, and legal evidence details.
- Public parcel summaries are stored in `parcels.public_parcel_summaries`, giving the public API a dedicated data-minimized projection instead of querying protected registry structures directly.
- Staff-only parcel draft geometry API and staff-console form now create `DRAFT` geometry versions in `parcels.parcel_geometry_versions`, preserving geometry history without publishing a current approved cadastral geometry.
- Staff console renders a low-bandwidth SVG preview of stored WKT geometry history for basic officer review. This is not a replacement for the future OpenLayers/GeoServer cadastral map.
- Draft geometry versions can now be submitted into an evidence-linked `PARCEL_GEOMETRY_APPROVAL` workflow. Approval atomically supersedes any prior current approved geometry and publishes exactly one approved current geometry version; rejection marks the proposal rejected without changing current geometry.
- Explicit staff-only parcel status transition endpoint backed by the `parcels.parcel_state_transitions` table, preventing arbitrary lifecycle changes.
- Durable workflow task scaffold in `workflow.tasks` for transitions that require approval. Approval-required parcel transitions now create an open task and leave parcel state unchanged.
- Staff-only workflow task listing and decision endpoint for the current parcel status-transition slice. Approved tasks atomically apply the pending status change; rejected tasks record the decision and leave parcel state unchanged.
- Maker-checker guard for approval-required parcel status transitions: the authenticated requester user ID is stored on the task and the same user cannot approve that task.
- Staff-only workflow task claim endpoint. A task must be claimed before decision, and only the claiming actor can approve or reject it.
- Workflow requester, claimer, and decider checks now use durable `identity.users.id` references; username/email strings remain display labels only.
- Workflow claiming now requires an active `identity.organization_memberships` record for the task's assigned role, initially `CADASTRAL_OFFICER`, and for parcel tasks the membership province/jurisdiction must match the parcel administrative unit or one of its ancestors.
- Workflow evidence links in `workflow.task_evidence_links`, allowing staff to attach a controlled evidence reference to a task before decision.
- Approval decisions now require at least one linked evidence record; rejection may proceed with a reason without mutating parcel state.
- Protected document metadata foundation in `documents.documents` and `documents.document_versions`, including classification, retention, access policy, checksum, malware-scan status, digital-signature status, and object-storage references.
- Immutable document version append endpoint; replacing evidence creates a new document version row and preserves prior version metadata.
- Server-side document access policy enforcement for protected metadata: creators can read their own records, workflow-scoped documents can be read by staff linked to a referencing workflow task, and owner-based listing filters out unreadable documents.
- Optional document custodian organization and role metadata, allowing active members of the custodian organization/role to read protected metadata and append immutable versions without granting blanket staff access.
- Data-minimized audit events for document metadata creation, metadata read, owner-list view, immutable version append, denied metadata read, and denied version append. Document audit events now include actor user ID, optional custodian organization ID, direct request remote IP when parseable, trusted-proxy-gated `X-Forwarded-For`, bounded user-agent, active registered device ID, and safe structured evidence metadata.
- Staff-only registered-device API for enrolling and listing devices used in audit traceability, with suspend/revoke/expire requests routed through maker-checker workflow before mutation.
- Staff console vertical slice for registered-device enrollment/listing, lifecycle approval-task creation, generic task claiming, evidence attachment, and workflow-type-aware decisions for parcel and device tasks.
- Staff console evidence-review panel for the selected workflow task, including explicit evidence refresh, visible evidence count before decision, and warning state when no evidence is linked.
- Backend regression coverage for the registered-device maker-checker lifecycle path: enrollment, revocation task creation, claim, evidence-required approval block, evidence attachment, approval, audit recording, and final device mutation.
- Checked-in OpenAPI contract slice for registered-device lifecycle workflow endpoints and schemas, with a contract smoke test protecting the documented paths and response shapes.
- `DOCUMENT` workflow evidence must reference an existing document metadata record and is limited to staff/legal/financial/protected classifications suitable for workflow review.
- The fictional local staff seed creates a fictional active cadastral-office organization membership so local development can exercise the workflow without granting production authority.
- Staff console support for loading open workflow tasks, claiming a task, and submitting approve/reject decisions with a required reason.

## Acceptance Notes

This is not yet the full Phase 2 product surface. Search by coordinates, standards-based map rendering, advanced topology validation, split/merge operations, and mobile survey capture remain future Phase 5 work.

The local HTTP `ROLE_STAFF` gate is intentionally coarse at this stage and only protects endpoint access. Workflow claiming now adds narrower active role-membership and parcel jurisdiction checks, but before production use this must be extended with ABAC using organization type, assignment policy, parcel sensitivity, transaction risk, and workflow state.

Public search returns only data-minimized parcel summary fields. It is not a registry certificate, title search, ownership confirmation, or legal-right determination.

Status transitions currently support immediate low-risk state movement, approval-required task creation, role-and-jurisdiction-scoped task claiming, evidence-linked approval, a controlled approval/rejection decision path, a same-user self-approval block, and a claimed-user-only decision check. Draft geometry capture stores historical geometry versions and approval now publishes current geometry only through an evidence-linked maker-checker workflow. Document metadata access now has an initial server-side policy, custodian role scope, registered-device verification, and enriched data-minimized audit event coverage. Registered-device suspend/revoke/expire requests now use the same evidence-linked maker-checker controls before mutation. Future cadastral/legal transitions must still add effective dates, appeal handling, finer role-specific scopes, document custody transfer approvals, deployment-specific trusted-proxy CIDR approval, and full policy-driven ABAC before any production registry-affecting publication.
