# Initial Threat Model

## Protected Assets

- Protected personal information.
- Legal and cadastral evidence.
- Parcel geometry history.
- Rights and restrictions.
- Payment and reconciliation records.
- Audit events.
- Credentials, tokens, and keys.

## Primary Threats

- Unauthorized legal or cadastral change.
- Insider misuse by staff or administrators.
- Compromised staff accounts.
- Public exposure of protected owner information.
- Forged documents or survey evidence.
- Payment callback replay.
- Audit-log tampering.
- Bulk scraping and uncontrolled geospatial export.
- Compromised field device synchronization.
- Supply-chain dependency compromise.

## Phase 1 Controls

- Server-side authorization defaults.
- BCrypt password hashing.
- Correlation IDs.
- Append-only audit table enforced by database trigger.
- No official-registry claims in public copy.
- Database schemas separated by module.
- Idempotency record table.
- CI runs backend tests, checked-in OpenAPI contract smoke checks, PostgreSQL/PostGIS-backed Flyway migration and API startup verification, frontend typecheck/build, npm moderate-or-higher dependency audit, and repository SBOM artifact generation on push and pull request.

## Residual Risks

- MFA is designed but not implemented in Phase 1.
- ABAC policy engine is not yet implemented.
- Workflow engine choice is still open.
- No production secrets manager is configured in Phase 1.
- CI still needs production-grade expansion for SAST, container scanning, signed artifacts, provenance attestations, and deployment-policy gates.
