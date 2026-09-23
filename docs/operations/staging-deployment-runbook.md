# Staging Deployment Runbook

## Purpose

This runbook prepares a staging environment for technical validation. Staging is not an official registry environment and must not contain production personal data or government secrets.

## Pre-Deployment Checklist

- Approved release bundle and image digest selected.
- Development seed disabled.
- Database credentials stored in the approved secret store.
- CORS origins restricted to staging portals.
- Backup and restore procedure rehearsed.
- Monitoring, logging and alert destinations configured.
- Pilot operational gates recorded for environment promotion when moving beyond staging.

## Smoke-Test Flow

1. Apply database migrations.
2. Start the API.
3. Confirm `/actuator/health/readiness` returns `UP`.
4. Confirm anonymous access to protected APIs returns unauthorized.
5. Confirm `/api/v1/platform/disclaimer` states the legal boundary.
6. Confirm `/api/v1/platform/pilot-readiness` requires staff authentication.
7. Confirm no development seed account is present unless this is an isolated local-only test.

## Rollback

1. Stop incoming traffic.
2. Preserve logs, audit events and deployment metadata.
3. Restore the previously approved image digest.
4. Restore database from the last accepted backup only after owner approval.
5. Record rollback evidence in the pilot readiness workflow when relevant.
