# Phase 12 Backup, Restore and Operational Resilience

## Outcome

Establish a repository-native backup and recovery baseline for controlled staging and pilot preparation. The platform must be able to demonstrate that registry, document and audit data can be protected, restored into an isolated environment and verified before any reconnection to users or integrations.

## Scope Boundary

Phase 12 does not provision a production backup provider, declare official RPO/RTO values or perform destructive restore operations. RPO/RTO values require explicit owner approval. The included scripts are intentionally non-destructive by default.

## Affected Modules

- PostgreSQL/PostGIS operational data.
- S3-compatible document object storage.
- Audit, payment, workflow and integration evidence.
- Deployment, CI and operational runbooks.

## Database Considerations

- PostgreSQL must be configured for encrypted backups and point-in-time recovery before pilot launch.
- Restore rehearsals must validate Flyway history, critical schemas, spatial metadata, audit continuity and idempotency tables.
- A restored environment must remain isolated until signed off by operations, security and business owners.
- No restore script may drop or overwrite a database without a separate, explicit operator-controlled procedure.

## API and Runtime Considerations

- API startup after restore must run in restricted or read-only verification mode until data integrity is accepted.
- Client traffic, integration callbacks and workflow workers must remain disabled until replay safety is approved.
- Notification failures must not block recovery validation, but failed queues must be counted and reconciled.

## Security Considerations

- Backups must be encrypted at rest and protected by managed keys with rotation.
- Restore credentials must be kept outside source control.
- Backup locations must use immutable retention where supported.
- Access to backup material, audit exports and restore environments must be logged and reviewed.
- Nonproduction restore datasets require masking or an approved protected-data handling exception.

## Privacy Considerations

- Restored pilot or test environments must not expose protected owner, identity, financial or document data to unauthorized users.
- Public parcel projections must be rebuilt from restored registry state instead of exposing registry tables.
- Legal holds and retention categories must survive restore and migration operations.

## Failure Modes

- Backup exists but cannot be restored.
- Database restores successfully but object documents are missing or checksums fail.
- PostGIS extension or spatial indexes are absent after restore.
- Workflow workers replay unsafe registry changes.
- Payment callbacks or integration events are duplicated after recovery.
- Audit continuity is broken or security logs are unavailable.
- Recovery point is selected without legal/business authorization.

## Acceptance Criteria

- Backup and recovery runbook defines incident sequence, isolation, validation and sign-off.
- Recovery policy documents PITR, immutable backup storage, object backup and restore ownership.
- Restore validation checklist covers database, spatial, document, audit, payment and workflow checks.
- Restore rehearsal script remains non-destructive by default and supports dump validation.
- CI validates required backup/recovery assets and safe script defaults.
- Release gates reference restore rehearsal as a pilot hard stop.

