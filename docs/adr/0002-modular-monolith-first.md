# ADR 0002: Modular Monolith First

## Status

Accepted for Phase 1.

## Context

The platform covers identity, parcels, cadastre, rights, payments, workflow, documents, notifications, reporting, and integrations. Splitting all bounded contexts into independent services before the domain is proven would create operational and consistency risk.

## Decision

Start as a modular monolith, with independently scalable platform services only where operationally justified.

Initial candidates for separately scalable services are GIS/tile serving, document processing, notification delivery, workflow workers, search indexing, integration adapters, audit ingestion, and analytics pipelines.

## Consequences

- Registry-affecting transactions can remain strongly consistent early.
- Deployment and observability are simpler during pilot preparation.
- Bounded modules must keep strict dependency rules to avoid a future extraction trap.
