# Pilot Readiness Runbook

## Purpose

This runbook supports a controlled pilot of the proposed land-information and workflow platform. It does not authorize official land registry operation.

## Go/No-Go Checklist

- Legal boundary disclaimer approved for the pilot.
- Pilot geography and participating institutions approved in writing.
- Development seed accounts disabled.
- User accounts created with least-privilege roles and jurisdiction scopes.
- Staff, surveyor, support and auditor training completed.
- Backup restore check completed in the pilot environment.
- CI pipeline green for the release candidate.
- Release bundle, SBOM and provenance artifacts retained.
- Sandbox integration mode accepted, or production credentials formally approved.
- Incident response contacts and support hours published.
- Rollback drill completed and evidence stored.
- Data migration trial reconciled with unresolved exceptions documented.

## Acceptance Workflow

1. Create a pilot readiness record in the staff console.
2. Attach readiness evidence references for charter, UAT, training, migration rehearsal, security checks, backup restore and incident response.
3. Create sign-off tasks for legal boundary, data protection, security, operations, migration, training, support and owner approval.
4. Claim and decide sign-off tasks through the workflow queue, with evidence attached to each task before approval.
5. Add pilot risks and mark each blocking risk as mitigated, accepted or closed before requesting final GO review.
6. Request final go/no-go review only after the readiness checks are complete.
7. Record final GO or NO-GO through the pilot workflow task. A GO decision is an operational pilot acceptance only; it is not official registry authorization.

## Stop Conditions

Pause the pilot if any of the following occur:

- A user-facing screen implies the platform is an official land registry without authorization.
- Protected ownership or identity data is exposed publicly.
- A legal or cadastral update bypasses configured human approval.
- A payment callback mutates registry state directly.
- A production integration is used without written authority and credentials.
- A compromised staff account, service account or field device is suspected.
- Audit events cannot be written or protected.
- Backup restore fails during readiness testing.
- Migration produces unresolved legally ambiguous ownership conflicts that are being treated as resolved.

## Rollback Procedure

1. Freeze new applications and field synchronization.
2. Preserve audit logs, workflow state, migration batches and object-storage evidence.
3. Export a read-only incident snapshot for authorized investigators.
4. Disable affected integrations or switch adapters back to sandbox mode.
5. Restore the last accepted database backup into a separate verification environment.
6. Compare application counts, parcel versions, document checksums and payment reconciliation records.
7. Obtain written approval before resuming pilot traffic.

## Incident Response

- Classify incidents as security, privacy, registry integrity, availability, payment or operational.
- Record correlation IDs, affected users, affected parcels, device IDs and integration references.
- Do not send sensitive owner or financial details over unsecured SMS or email.
- Notify the approved pilot owner and security contact before external disclosure.
- Preserve evidence under legal hold when disputes, court matters or fraud allegations are involved.

## Readiness Endpoint

Staff users can call:

```text
GET /api/v1/platform/pilot-readiness
```

The response reports pilot preparation phase, legal status, disclaimer and readiness checks. It is not public and must remain protected by staff authentication.
