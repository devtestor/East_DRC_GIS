# Phase 8 Pilot Preparation Plan

## Outcome

Prepare a controlled pilot package for the Eastern DRC Land Parcel and GIS Management Platform without representing the system as an official land registry. Phase 8 is complete when pilot scope, operational controls, training, migration rehearsal, acceptance criteria, stop conditions and rollback procedures are documented and testable.

## Scope Boundary

The pilot is a proposed land-information and workflow evaluation. It must not issue legally valid land titles, silently alter official records, expose protected ownership data publicly, or connect to production government services without written authorization and approved credentials.

## Conservative Assumptions

- Pilot geography is not yet formally approved, so all examples remain fictional.
- Production RTO and RPO are not owner-approved, so the plan presents options rather than final values.
- Civil identity, treasury, court, tax, urban planning, payment and digital-signature integrations remain sandbox adapters.
- AI or anomaly detection outputs are investigation leads only and cannot determine ownership, legal rights or fraud.
- Development seed accounts must be disabled in any shared pilot environment.

## Affected Modules

- Platform health: pilot readiness endpoint for staff checks.
- Staff console: pilot readiness panel for operational review.
- Pilot readiness: formal acceptance records, sign-offs, risks, evidence and go/no-go workflow.
- Operations documentation: pilot runbook, UAT scenarios and migration rehearsal checklist.
- Security documentation: approval gates remain enforced by workflow, role and maker-checker controls.

## Pilot Workstreams

1. Governance
   - Confirm approving institutions and pilot owner.
   - Confirm data-sharing authority and privacy obligations.
   - Confirm legal wording for all public and staff-facing disclaimers.
   - Confirm named escalation contacts.

2. Geography and Data
   - Select limited pilot area using approved administrative boundaries.
   - Inventory source records, shapefiles, scanned documents and spreadsheets.
   - Run trial migration into a nonproduction pilot environment.
   - Preserve source lineage, validation results and unresolved exceptions.

3. Users and Training
   - Register pilot staff, surveyors, support users and auditors with least-privilege roles.
   - Train users on legal boundaries, protected data handling, evidence upload and approval workflows.
   - Run tabletop exercises for disputes, mistaken uploads, payment outages and compromised devices.

4. Operations
   - Confirm backup, restore and release gates.
   - Confirm monitoring, incident response and support hours.
   - Run migration rehearsal, UAT and rollback drills.
   - Record go/no-go decision evidence.

5. Acceptance Workflow
   - Create one pilot readiness record per controlled pilot.
   - Attach evidence references for charter, training, migration rehearsal, security sign-off, backup restore, UAT and incident response.
   - Create required sign-off tasks for legal boundary, data protection, security, operations, migration, training, support and owner approval.
   - Track blocking risks until they are mitigated, accepted or closed.
   - Request final go/no-go only after all required sign-offs are approved and no blocking risks remain open.

## Phase 8 Acceptance Criteria

- Pilot scope, roles, training, support model, stop conditions and rollback procedures are documented.
- Staff-only readiness endpoint returns legal boundary, approval, adapter and pilot-scope signals.
- Staff console displays readiness checks without exposing protected personal data.
- Pilot acceptance workflow records sign-offs, risks, evidence and final go/no-go decisions with workflow tasks.
- Final go approval is blocked until all required sign-offs are approved and blocking risks are no longer open.
- Migration rehearsal checklist preserves source lineage and unresolved legal exceptions.
- UAT scenarios cover citizen, staff, survey, dispute, document, payment and security flows.
- Tests pass for backend readiness logic and frontend type checking.
- CI remains green after the Phase 8 changes.

## Open Decisions

- Exact pilot geography and administrative authority.
- Approved RTO and RPO targets.
- Whether pilot payments remain fully sandboxed or use a limited approved provider.
- Data retention period for pilot evidence and mobile offline packages.
- Final language list beyond French and Kiswahili.
