# Eastern DRC Land Parcel and GIS Management Platform

This repository contains the Phase 1 foundation for a proposed land-information and workflow platform for Eastern Democratic Republic of the Congo.

The system is not an official Congolese land registry. Generated records, identifiers, certificates, approvals, and workflows are not legally valid land-title instruments unless and until competent public institutions formally authorize the relevant operating mode.

## Architecture Direction

- Modular monolith first, with independently scalable platform services where justified.
- Backend: Java 21 and Spring Boot.
- Web: Next.js, React, TypeScript.
- GIS: PostgreSQL/PostGIS, GeoServer-compatible services, OGC-oriented APIs.
- Workflow: workflow-engine abstraction first; Temporal or Camunda decision is deferred to an ADR before Phase 4.
- Data posture: privacy by design, public projections separate from protected registry data.

## Phase 1 Scope

Phase 1 establishes the platform foundation:

- Monorepo structure.
- Spring Boot API scaffold.
- Next.js portal scaffolds.
- PostgreSQL/PostGIS local dependency definition.
- Database schemas and baseline migrations.
- Identity and audit primitives.
- Security, architecture, threat model, and risk documentation.
- CI skeleton.

## Local Development

Prerequisites:

- Java 21.
- Node.js 20 or newer.
- Docker with Compose.

The current workspace has a read-only placeholder `.git` directory, so Git initialization may need environment cleanup before normal source-control workflows can begin.

Versioned releases use GitHub Container Registry through `.github/workflows/release.yml`. Configure the repository `pilot` environment with required reviewers before pushing a `v*.*.*` tag. The workflow uses the short-lived GitHub Actions token and does not require a long-lived registry password.

```bash
docker compose up -d postgres
```

Backend and frontend dependency installation requires network access to package registries.

## Legal Boundary

The proposed UPI is configurable and versioned. It must not be presented as an approved national standard. Public map views must not expose protected landholder information. AI or automated validation may support review but must not make final legal-right or ownership decisions.
