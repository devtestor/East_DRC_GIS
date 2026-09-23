# Phase 9 Staging and Deployment Readiness

## Outcome

Prepare the platform for a controlled staging deployment and later pilot promotion without assuming legal authority, production credentials or operational capacity that has not been approved.

## Scope Boundary

Phase 9 does not deploy an official land registry. It produces deployable artifacts, environment contracts, smoke-test topology, infrastructure placeholders and promotion gates that require explicit approval before use.

## Deliverables

- Staging environment variable contract.
- Docker Compose staging smoke-test topology.
- Kubernetes API baseline with non-root runtime, probes and secret references.
- Terraform variable contract and guardrails.
- Deployment readiness CI check.
- Staging runbook and promotion checklist.
- Staging smoke-test automation for local Compose and deployed API probes.

## Required Owner Decisions

- Hosting provider and approved region.
- Whether production will use Kubernetes or a simpler managed container runtime.
- RTO/RPO targets.
- Secrets manager and key rotation owner.
- Domain names and TLS certificate owner.
- Object-storage provider and retention policy.
- Monitoring/SIEM ownership.

## Acceptance Criteria

- Development seed is disabled in staging/pilot templates.
- Staging/pilot templates reference secrets instead of embedding real credentials.
- API deployment includes health probes and non-root container controls.
- Promotion to pilot remains approval gated.
- Documentation explicitly preserves the legal boundary.
- Smoke tests verify readiness, public disclaimer and protected endpoint behavior.
