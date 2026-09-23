# Eastern DRC Land Parcel and GIS Management Platform

This repository contains a phased implementation of a proposed land-information and workflow platform for Eastern Democratic Republic of the Congo. The current build has completed foundation, parcel registry, rights, documents, workflow, survey, integration and operational hardening slices, and is now preparing the controlled pilot package.

The system is not an official Congolese land registry. Generated records, identifiers, certificates, approvals, and workflows are not legally valid land-title instruments unless and until competent public institutions formally authorize the relevant operating mode.

## Architecture Direction

- Modular monolith first, with independently scalable platform services where justified.
- Backend: Java 21 and Spring Boot.
- Web: Next.js, React, TypeScript.
- GIS: PostgreSQL/PostGIS, GeoServer-compatible services, OGC-oriented APIs.
- Workflow: workflow-engine abstraction first; Temporal or Camunda decision is deferred to an ADR before Phase 4.
- Data posture: privacy by design, public projections separate from protected registry data.

## Implemented Scope

- Monorepo structure with Spring Boot API, Next.js portals and Flutter field-app scaffold.
- PostgreSQL/PostGIS migrations for identity, audit, administration, parcels, workflow, documents, parties, rights, disputes, surveys, notifications and payment reconciliation.
- Configurable administrative geography, proposed UPI handling, parcel lifecycle and geometry versioning.
- Human approval workflows with maker-checker controls for sensitive operations.
- Protected document metadata, field-device enrollment, survey operations and sandbox integration adapters.
- CI checks for backend tests, frontend builds, OpenAPI contracts, dependency audit, safe defaults, container scan, SBOM and release provenance.
- Phase 8 pilot preparation documents and staff-only readiness checks.

## Local Development

Prerequisites:

- Java 21.
- Node.js 20 or newer.
- Docker with Compose.

Versioned releases use GitHub Container Registry through `.github/workflows/release.yml`. Configure the repository `pilot` environment with required reviewers before pushing a `v*.*.*` tag. The workflow uses the short-lived GitHub Actions token and does not require a long-lived registry password.

```bash
docker compose up -d postgres
```

Backend and frontend dependency installation requires network access to package registries.

## Legal Boundary

The proposed UPI is configurable and versioned. It must not be presented as an approved national standard. Public map views must not expose protected landholder information. AI or automated validation may support review but must not make final legal-right or ownership decisions.

## Pilot Preparation

Phase 8 materials are in:

- `docs/architecture/phase-8-plan.md`
- `docs/operations/pilot-readiness-runbook.md`
- `docs/operations/pilot-uat-scenarios.md`
- `docs/operations/migration-rehearsal-checklist.md`
