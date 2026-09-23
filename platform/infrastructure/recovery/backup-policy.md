# Backup and Recovery Control Baseline

This policy is a technical baseline for staging and controlled pilot readiness. It does not declare final RPO or RTO values; those must be approved by the business, legal/institutional and operations owners.

## Protected Data Sets

| Data set | Minimum control |
| --- | --- |
| PostgreSQL/PostGIS registry database | Encrypted full backups, WAL archiving for PITR, restore rehearsal, integrity checks |
| Document object storage | Versioning, encryption, checksum verification, immutable retention where supported |
| Audit/security logs | Append-only export or replicated immutable storage, independent access controls |
| Release artifacts and SBOMs | Artifact retention, provenance attestations, checksums |
| Secrets and keys | Managed secrets store, key rotation, access logging |

## Backup Requirements

- Backups must be encrypted before leaving the source service boundary.
- Backup storage must be isolated from the primary runtime account/project.
- Deletion protection or immutable retention must be enabled where the provider supports it.
- PostgreSQL WAL/PITR configuration must be tested before pilot launch.
- Backup jobs must emit metrics and alerts for success, failure, age and size anomalies.
- Failed backup jobs must create an operations incident when they exceed the approved tolerance.

## Restore Requirements

- Restores must first target an isolated recovery environment.
- Registry-affecting writes, workflow workers and external integration consumers must stay disabled until approval.
- Restore validation must include database, spatial, documents, audit, payments, workflow and public projections.
- Any unrecoverable gap must be recorded as an incident finding before pilot or production traffic resumes.

## Approval Requirements

- Operations owner approves the recovery point and technical restore completion.
- Security owner approves access, key handling, log integrity and isolation.
- Data owner approves privacy controls and masking for nonproduction use.
- Business/legal owner approves resuming registry-affecting workflows.

## Explicit Non-Goals

- No script in this repository may silently overwrite or drop a database.
- No payment, workflow or legal registry event may be replayed without idempotency checks and approval.
- No restored protected data may be used for demonstrations, training or analytics without approved masking and access control.

