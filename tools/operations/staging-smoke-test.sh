#!/usr/bin/env bash
set -euo pipefail

api_url="${API_URL:-http://localhost:8080}"
max_attempts="${SMOKE_MAX_ATTEMPTS:-60}"
sleep_seconds="${SMOKE_SLEEP_SECONDS:-2}"

require_status() {
  local expected_status="$1"
  local path="$2"
  local description="$3"
  local status
  status="$(curl -sS -o /tmp/edrc-smoke-response.txt -w '%{http_code}' "$api_url$path")"
  if [ "$status" != "$expected_status" ]; then
    echo "Smoke check failed: $description expected HTTP $expected_status but got $status" >&2
    cat /tmp/edrc-smoke-response.txt >&2 || true
    exit 1
  fi
}

wait_for_readiness() {
  local attempt=1
  while [ "$attempt" -le "$max_attempts" ]; do
    if curl -fsS "$api_url/actuator/health/readiness" | grep -q '"status":"UP"'; then
      return 0
    fi
    sleep "$sleep_seconds"
    attempt=$((attempt + 1))
  done
  echo "API readiness did not become UP after $max_attempts attempts" >&2
  exit 1
}

wait_for_readiness
require_status "200" "/actuator/health/readiness" "readiness endpoint"
require_status "401" "/api/v1/platform/pilot-readiness" "staff-only pilot readiness endpoint"
require_status "401" "/api/v1/pilots" "staff-only pilot API"

disclaimer="$(curl -fsS "$api_url/api/v1/platform/disclaimer")"
printf '%s\n' "$disclaimer" | grep -qi 'proposed land-information and workflow system'
printf '%s\n' "$disclaimer" | grep -qi 'not an official land registry'

echo "Staging smoke checks passed for $api_url"
