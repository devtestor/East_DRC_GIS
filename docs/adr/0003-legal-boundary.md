# ADR 0003: Legal Boundary and Public Claims

## Status

Accepted for Phase 1.

## Context

The product is a proposed land-information and workflow system. It must not claim official registry authority, legal title issuance, approved national UPI status, or government API access without formal authorization.

## Decision

All generated UX and API surfaces must preserve the legal boundary:

- UPI values are proposed, configurable, and versioned.
- Certificates and documents are operational artifacts unless approved by competent institutions.
- AI and automated validation provide decision support only.
- Protected landholder information must not be exposed in public projections.
- External government integrations start as interfaces with sandbox or mock implementations.

## Consequences

Phase 1 includes a public disclaimer endpoint and frontend copy that avoids official-registry claims.
