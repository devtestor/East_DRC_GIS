# Backup and Recovery Runbook

This is an operational template. It is not a substitute for an approved disaster-recovery plan.

## Operating principles

- Do not invent or assume final RPO/RTO values. Record approved options and owner decisions.
- Restore into an isolated environment first; do not reconnect clients or integrations until validation is approved.
- Treat audit, payment, workflow and document evidence as registry-critical data.
- Never replay legal registry changes solely because a queue or backup contains an event.
- Keep recovery credentials, backup encryption keys and object-store credentials outside source control.

## Before an incident

- Enable PostgreSQL point-in-time recovery and encrypted backups.
- Store backups in a separate account or project with immutable retention.
- Version and replicate encrypted document objects.
- Store recovery credentials and keys in the approved secrets manager.
- Record approved RPO/RTO values and the recovery decision-maker.
- Perform and record a restore rehearsal at least before pilot launch and after material platform changes.
- Alert on failed backups, stale backups, unusually small backups and missing WAL/PITR segments.
- Confirm restore operators have break-glass access that is logged and time-bounded.

## Recovery sequence

1. Declare the incident and record the incident/correlation identifier.
2. Freeze registry-affecting writes if integrity is uncertain; preserve audit and security logs.
3. Confirm the recovery point and obtain the required approval.
4. Restore PostgreSQL to an isolated recovery environment.
5. Validate Flyway schema version, foreign keys, spatial indexes, audit rows, payment callbacks, and reconciliation entries.
6. Restore document objects and verify checksums and malware-scan metadata.
7. Start the API in read-only or restricted mode for verification.
8. Compare parcel counts, current geometry counts, public projections, open applications, and pending payments with the last trusted report.
9. Obtain business and security sign-off before reconnecting clients.
10. Reconcile queued integrations and notifications idempotently; never replay legal changes without workflow evidence and authorization.

## Minimum validation evidence

- Database backup identifier, WAL/PITR recovery point and restore target.
- Flyway version, schema list and application startup result.
- PostGIS extension and spatial-index verification.
- Counts for active parcels, current approved geometries, open applications, pending payments and unresolved disputes.
- Sample document checksum verification across multiple document classifications.
- Audit-event continuity check before and after recovery.
- Confirmation that workflow workers and integration consumers remained disabled until approval.
- Sign-off from operations, security, data and business/legal owners.

## Restore rehearsal command

Use the non-destructive helper for logical backup validation:

```bash
DB_PASSWORD=... tools/operations/backup-restore-check.sh
```

The helper creates a temporary logical dump and verifies that backup material is non-empty. It does not create, drop or overwrite databases.

## Exit criteria

Recovery is complete only when integrity checks pass, monitoring is healthy, audit continuity is preserved, and the incident owner records any lost or replayed work.
