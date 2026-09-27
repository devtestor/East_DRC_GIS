# Phase 15: controlled privacy export workflow baseline

## Outcome

Phase 15 introduces controlled document export requests for protected registry and evidence information. It does not generate bulk export files yet. The phase establishes the approval workflow and audit trail that future export generation must use.

## Affected modules

- `governance`: export request lifecycle and review completion.
- `documents`: governed export source records.
- `workflow`: security-officer export review tasks.
- `audit`: export request and review audit events.
- `security`: staff-only governance API route and high-risk workflow policy.

## Database changes

- New `governance.export_requests` table.
- Export statuses:
  - `BLOCKED`
  - `PENDING_REVIEW`
  - `APPROVED`
  - `REJECTED`
- Optional link to `workflow.tasks`.
- Indexes for document lookup and pending status review.
- Unique workflow-task link to prevent one approval task being reused for multiple export requests.

## API changes

- `POST /api/v1/governance/export-requests`
- `GET /api/v1/governance/export-requests/{requestId}`
- `POST /api/v1/governance/export-requests/{requestId}/review-completion`

All routes require authenticated staff access.

## Security and privacy considerations

- Sensitive document exports require redaction planning and approval.
- Legal-hold blockers prevent approval.
- Export approval requires a workflow task assigned to `SECURITY_OFFICER`.
- Export request creation and review completion are audited as security events.
- The API records approval state only; it does not expose protected document content.

## Failure modes

- Missing document: request creation fails.
- Redaction not planned for sensitive data: request is blocked and no workflow task opens.
- Legal hold: request is blocked.
- Workflow task not approved/rejected: review completion is rejected.
- Policy still blocks export after workflow approval: export approval is rejected.

## Acceptance criteria

- Export request records are persisted.
- Blocked requests do not open approval tasks.
- Reviewable requests open `DOCUMENT_EXPORT_REVIEW` workflow tasks.
- The high-risk workflow policy registers document export approval.
- Approval cannot be recorded until the workflow task is approved.
- Tests and CI guardrails pass.
