# Phase 7 Operational Hardening

This phase establishes safer deployment defaults and the operational controls required before a pilot. It does not authorize production registration, official title issuance, or connection to government systems.

## Implemented controls

- Development account seeding is disabled by default. Local development must explicitly set `DEV_SEED_ENABLED=true`.
- CORS origins are configured through `CORS_ALLOWED_ORIGINS`; production must use an explicit allow-list of approved HTTPS origins.
- API responses include restrictive content-security, frame, referrer, permissions, and transport-security headers.
- Multipart uploads and request headers have bounded sizes to reduce resource-exhaustion risk.
- Actuator endpoints remain limited to health, info, and Prometheus and must be network-restricted in production.
- Payment and identity integrations remain sandbox adapters until institutional authorization, credentials, signed callbacks, data-sharing approval, and operational ownership exist.

## Production checklist

Before a pilot deployment, the release owner must confirm:

1. `DEV_SEED_ENABLED=false` and no development credentials are present.
2. `CORS_ALLOWED_ORIGINS` contains only approved HTTPS origins.
3. Database credentials and encryption keys come from a secrets manager.
4. TLS termination, trusted proxy CIDRs, and forwarded-header behavior are reviewed by security staff.
5. Actuator access is restricted to the operations network and authenticated monitoring.
6. PostgreSQL point-in-time recovery and encrypted immutable backups have passed a restore test.
7. Object-storage versioning, retention, legal hold, and malware scanning are configured.
8. Logs and security events are routed to the approved monitoring/SIEM destination without personal-document contents or tokens.
9. A rollback image and database migration recovery plan are rehearsed.
10. Provider-specific integration approvals are recorded before any production adapter is enabled.

## Backup and restore rehearsal

The production runbook must capture the approved RPO/RTO, backup owner, encryption key owner, and restore approver. A restore rehearsal must validate migrations, audit immutability, document references, payment reconciliation records, and public parcel projections. RPO/RTO values are intentionally not invented by this repository and require owner approval.
