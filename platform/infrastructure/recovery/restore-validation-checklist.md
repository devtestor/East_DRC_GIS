# Restore Validation Checklist

Use this checklist after restoring into an isolated environment. Record the incident ID, backup identifier, restore target, responsible operator and approvers in the incident record.

## Database Integrity

- Confirm Flyway schema history is present and at the expected version.
- Confirm required schemas exist: identity, administration, parcels, cadastre, parties, rights, transactions, workflows, documents, payments, disputes and audit.
- Confirm foreign-key checks pass.
- Confirm application migrations start without applying unexpected changes.
- Confirm idempotency records are present for payment callbacks and committed transactions.

## Spatial Integrity

- Confirm PostGIS extension is installed.
- Confirm parcel geometry tables contain exactly one current approved geometry per active parcel.
- Confirm spatial indexes exist on geometry columns.
- Confirm invalid current parcel geometries are zero or have approved exception records.
- Confirm public parcel projections can be rebuilt without exposing protected registry data.

## Document Integrity

- Confirm object buckets or prefixes are restored from the selected backup point.
- Confirm sample document checksums match database metadata.
- Confirm malware-scan metadata and legal-hold flags are preserved.
- Confirm older document versions remain available and are not replaced in place.

## Audit, Security and Privacy

- Confirm audit events are readable and append-only controls are restored.
- Confirm privileged restore access is logged.
- Confirm restored protected data is masked or isolated according to the approved recovery purpose.
- Confirm no application logs contain passwords, tokens, private keys, full identity documents or raw financial data.

## Workflow, Payments and Integrations

- Confirm workflow workers are disabled until replay is approved.
- Confirm pending payment, reconciliation and unmatched-payment states are counted.
- Confirm integration outbox/dead-letter queues are reconciled before consumers resume.
- Confirm notification retries are safe and do not include sensitive ownership details.

## Sign-Off

- Operations owner:
- Security owner:
- Data owner:
- Business/legal owner:
- Resume decision and timestamp:

