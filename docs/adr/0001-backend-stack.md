# ADR 0001: Backend Stack

## Status

Accepted for Phase 1.

## Context

The repository had no existing application stack. The platform requires strong validation, transaction boundaries, auditability, database constraints, OpenAPI documentation, PostGIS access, durable integrations, and long-term maintainability for a government-grade domain.

## Decision

Use Java 21 with Spring Boot for the backend modular monolith.

## Consequences

- Spring Security, Bean Validation, Spring Data JPA, Flyway, and Actuator provide a mature foundation.
- Module boundaries will be enforced initially through package structure, schemas, tests, and review discipline.
- External integrations must be hidden behind adapters.
- Legal and cadastral domain rules must not live only in controllers.
