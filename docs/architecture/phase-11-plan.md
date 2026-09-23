# Phase 11 Observability and Runtime Operations

## Outcome

Provide baseline monitoring, alerting, dashboard and log-safety assets for staging and controlled pilot operation.

## Scope Boundary

Phase 11 does not connect a live SIEM, paging provider or production observability platform. It provides repository-native assets and guardrails that operators can install once monitoring ownership and destinations are approved.

## Deliverables

- Prometheus scrape configuration.
- Prometheus alert rules.
- Grafana dashboard JSON for API runtime.
- Grafana dashboard JSON for pilot operations.
- Log redaction policy.
- CI observability guardrail.
- Operations runbook updates.

## Acceptance Criteria

- Metrics scrape `/actuator/prometheus`.
- Alerts cover availability, 5xx rate, latency, JVM heap and database pool pressure.
- Dashboards avoid protected personal data and financial details.
- Log policy explicitly bans tokens, passwords, private keys, identity documents and raw financial data.
- CI validates observability assets exist and contain required guardrails.
