# Phase 14: data governance and retention enforcement baseline

## Outcome

Phase 14 establishes the first enforceable data-governance layer for documents and evidence records. It does not introduce destructive deletion jobs. Instead, it creates the policy records and service decisions that future retention, archival, privacy export and legal-hold workflows must call before acting on protected information.

## Affected modules

- `governance`: retention policies, legal holds and privacy-export decisions.
- `documents`: governed target records for the first vertical slice.
- `audit`: legal-hold placement and release events.
- `platform/ci`: repository guardrails for governance controls.

## Database changes

- New `governance` schema.
- New `governance.retention_policies` table.
- New `governance.legal_holds` table.
- Partial unique index preventing more than one active legal hold for the same governed target.
- Seeded fictional/default retention categories:
  - `LEGAL_RECORD`
  - `IDENTITY_RECORD`
  - `FINANCIAL_RECORD`
  - `OPERATIONAL_RECORD`
  - `PUBLIC_RECORD`

## Security and privacy considerations

- Legal holds are append-style governance records and are audited.
- Protected, legal, financial and security-classified documents require redaction and approval before export.
- Active legal holds block export and automated disposition.
- Missing retention policy blocks automated disposition by default.
- This phase intentionally avoids automated destruction of records.

## Failure modes

- Missing policy: disposition is blocked and approval is required.
- Active legal hold: disposition and export are blocked.
- Sensitive export without redaction: blocked.
- Sensitive export without approval: blocked.
- Attempted duplicate active hold: blocked by a database unique partial index.

## Acceptance criteria

- Retention policy and legal-hold tables exist through migration.
- Automated disposition is denied for active legal holds.
- Sensitive exports require approval and redaction.
- Public records can be disposition-eligible only when policy allows it and no blockers exist.
- Legal-hold placement is audited.
- Automated tests pass.
- CI verifies governance assets and tests are present.
