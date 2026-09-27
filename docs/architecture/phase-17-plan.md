# Phase 17: staff-console governed export operations

## Outcome

Phase 17 exposes the governed export workflow in the staff console. It does not bypass backend authorization, workflow approval, token expiry or audit controls.

## Affected modules

- `staff-console`: governed export request, review completion, package generation and token-bound download UI.
- `governance`: existing API endpoints are consumed by staff users.
- `workflow`: `DOCUMENT_EXPORT_REVIEW` tasks remain the human approval gate.

## UI flow

1. Staff user creates a governed export request for a source document.
2. Sensitive exports open a `DOCUMENT_EXPORT_REVIEW` workflow task.
3. Security officer claims the task, attaches evidence and approves/rejects it in the workflow panel.
4. Staff user records final governance review completion.
5. Approved requests can generate a single export package.
6. The one-time displayed delivery token is copied into the download control.
7. Download requires authentication and the unexpired token.

## Security and privacy considerations

- The UI does not expose protected content until the backend package endpoint allows download.
- Approval and package generation remain separate actions.
- The delivery token is displayed only from the package generation response.
- The UI warns staff not to place sensitive ownership, legal, financial or identity details in unsecured SMS/email.
- All server-side checks remain authoritative.

## Acceptance criteria

- Staff can create an export request from the console.
- Staff can load request/package status.
- `DOCUMENT_EXPORT_REVIEW` tasks are recognizable in the workflow panel.
- Staff can complete governance review only through the backend endpoint.
- Staff can generate and download token-bound packages after approval.
- Frontend typecheck/build and governance guard pass.
