# Log Redaction Policy

## Never Log

- Passwords.
- Access tokens.
- Refresh tokens.
- API keys.
- Private keys.
- Database passwords.
- Full identity-document numbers.
- Full financial account details.
- Raw payment-provider payloads containing personal or financial data.
- Full landholder personal details unless explicitly approved for a protected audit event.

## Required Logging Context

- Correlation ID.
- Authenticated actor ID when available.
- Target entity type and UUID when applicable.
- Workflow task ID for approval decisions.
- Integration provider name without secret material.
- Device ID only after validation against registered active devices.

## Safe Operational Events

- Startup and readiness state.
- Migration version and status.
- Integration adapter mode, for example `sandbox` or `disabled`.
- Workflow state transitions without sensitive party details.
- Payment callback status without raw payload.
- Document metadata status without document contents.

## Incident Handling

When sensitive material appears in logs:

1. Preserve the affected log range under restricted access.
2. Rotate any exposed credentials.
3. Record a security incident.
4. Review audit events and access logs for misuse.
5. Add a regression guard or logging test before closing the incident.
