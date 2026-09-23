# Observability Runbook

## Purpose

This runbook describes the baseline monitoring and alerting approach for staging and controlled pilot operations.

## Metrics

The API exposes Prometheus metrics at:

```text
/actuator/prometheus
```

The staging scrape example is:

```text
platform/observability/prometheus/prometheus.yml
```

## Dashboards

Baseline dashboards are stored as JSON:

- `platform/observability/grafana/dashboards/api-runtime.json`
- `platform/observability/grafana/dashboards/pilot-operations.json`

Dashboard publication must be restricted to authorized staff and operators.

## Alerts

Baseline alert rules are in:

```text
platform/observability/prometheus/alerts.yml
```

Before pilot operation, owners must approve alert routing, escalation schedule and severity mapping.

## Log Safety

Follow:

```text
platform/observability/log-redaction-policy.md
```

Logs must not contain passwords, tokens, private keys, full identity documents, raw payment payloads or protected owner details.

## Triage

1. Check API readiness and liveness.
2. Check recent deployment and migration events.
3. Check API 5xx rate and p95 latency.
4. Check database pool pressure.
5. Check workflow backlog and pilot gates.
6. Preserve correlation IDs for incident analysis.
