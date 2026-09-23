# Phase 6 Integration Foundation

The platform now defines provider-independent contracts for payment initiation and identity verification.
Local sandbox adapters are intentionally non-authoritative:

- Sandbox payment returns `PENDING`, never `PAID`.
- Sandbox identity verification returns `PENDING_HUMAN_REVIEW`, never official verification.
- Production adapters require approved institutional authorization, credentials, callback signing,
  reconciliation rules, data-sharing approval, and operational ownership.

Planned adapters include civil identity, mobile money, banks, treasury, courts, tax, urban planning,
SMS/email, and digital signatures. Provider callbacks must use idempotency records and must not directly
commit legal registry changes.
