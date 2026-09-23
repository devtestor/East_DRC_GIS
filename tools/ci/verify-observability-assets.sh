#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

test -s "$root_dir/platform/observability/prometheus/prometheus.yml"
test -s "$root_dir/platform/observability/prometheus/alerts.yml"
test -s "$root_dir/platform/observability/grafana/dashboards/api-runtime.json"
test -s "$root_dir/platform/observability/grafana/dashboards/pilot-operations.json"
test -s "$root_dir/platform/observability/log-redaction-policy.md"
test -s "$root_dir/docs/operations/observability-runbook.md"

grep -q '/actuator/prometheus' "$root_dir/platform/observability/prometheus/prometheus.yml"
grep -q 'ApiInstanceDown' "$root_dir/platform/observability/prometheus/alerts.yml"
grep -q 'ApiHighServerErrorRate' "$root_dir/platform/observability/prometheus/alerts.yml"
grep -q 'ApiHighLatencyP95' "$root_dir/platform/observability/prometheus/alerts.yml"
grep -q 'ApiJvmMemoryPressure' "$root_dir/platform/observability/prometheus/alerts.yml"
grep -q 'ApiDatabasePoolPressure' "$root_dir/platform/observability/prometheus/alerts.yml"
grep -q 'EDRC Land GIS API Runtime' "$root_dir/platform/observability/grafana/dashboards/api-runtime.json"
grep -q 'EDRC Land GIS Pilot Operations' "$root_dir/platform/observability/grafana/dashboards/pilot-operations.json"
grep -qi 'Never Log' "$root_dir/platform/observability/log-redaction-policy.md"
grep -qi 'Access tokens' "$root_dir/platform/observability/log-redaction-policy.md"
grep -qi 'Private keys' "$root_dir/platform/observability/log-redaction-policy.md"

for forbidden in password token secret private_key access_token refresh_token db_password; do
  if grep -RIn "\"$forbidden\"" "$root_dir/platform/observability/grafana" "$root_dir/platform/observability/prometheus"; then
    echo "Observability assets include forbidden sensitive field name: $forbidden" >&2
    exit 1
  fi
done

echo "Observability asset checks passed"
