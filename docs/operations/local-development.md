# Local Development

## Dependencies

- Java 21.
- Node.js 20 or newer.
- Docker with Compose.

If the system default Java is newer than 21, run backend checks with:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon :apps:api:test
```

## Start Local Database

```bash
docker compose up -d postgres
```

The database image includes PostGIS. Application migrations are under `apps/api/src/main/resources/db/migration`.

If port `5432` is already in use:

```bash
POSTGRES_PORT=55432 docker compose up -d postgres
DB_PORT=55432 JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew --no-daemon :apps:api:bootRun
```

## Continuous Integration

The checked-in GitHub Actions workflow runs on push and pull request. It currently performs:

- registered-device OpenAPI contract smoke checks;
- backend API tests;
- PostgreSQL/PostGIS-backed Flyway migration and API startup verification;
- frontend workspace type checking and production builds;
- npm dependency audit for moderate-or-higher advisories;
- repository SBOM artifact generation.
- safe-default and credential-pattern checks;
- CodeQL analysis for Java and TypeScript;
- Trivy configuration scanning for high and critical findings.

Future hardening still needs signed artifacts, provenance attestations, and deployment gates. The backup script at `tools/operations/backup-restore-check.sh` creates a non-destructive logical backup for a separately approved restore rehearsal.

## Local Staff Account

When `DEV_SEED_ENABLED=true`, the API creates a fictional local staff account if it does not already exist:

- Email: `phase2.staff@example.test`
- Password: `ChangeMe-Phase2-Local!`

This account is for local development only. Disable it outside local development with `DEV_SEED_ENABLED=false`.

Local test URLs:

- API: `http://localhost:8080`
- API health: `http://localhost:8080/actuator/health`
- Citizen portal: `http://localhost:3001`
- Staff console: `http://localhost:3002`
- Staff login: `phase2.staff@example.test` / `ChangeMe-Phase2-Local!`

Dispute lifecycle API examples are under `/api/v1/parcels/{parcelId}/disputes`: hearings, decision requests and decisions, reopen requests and decisions, and appeals. All are staff-protected and use fictional local data only.

The staff console at `apps/staff-console` can use this account to create administrative units and draft parcels against the local API.

## Known Workspace Issue

The current workspace contains a read-only placeholder `.git` directory. It is not a valid Git repository, so normal `git status`, commits, and CI trigger behavior require environment cleanup or a fresh clone directory.
