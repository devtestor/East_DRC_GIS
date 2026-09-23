# Observability Baseline

This directory contains repository-native observability assets for staging and controlled pilot operations.

The assets are safe defaults only. They do not configure a live SIEM, production credentials, pager rotation or government monitoring integration.

## Contents

- `prometheus/prometheus.yml`: staging scrape configuration for the API.
- `prometheus/alerts.yml`: alert rules for API availability, latency, error rate, JVM memory and database pool pressure.
- `grafana/dashboards/api-runtime.json`: API runtime dashboard.
- `grafana/dashboards/pilot-operations.json`: pilot workflow and readiness dashboard.
- `log-redaction-policy.md`: sensitive logging and event-handling policy.

## Required External Decisions

- Metrics retention period.
- Alert routing and on-call schedule.
- SIEM destination and event taxonomy.
- Production log storage provider and encryption keys.
- Dashboard publication audience.
