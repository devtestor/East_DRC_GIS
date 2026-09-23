#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
compose_file="$root_dir/platform/infrastructure/compose/staging-compose.yml"
project_name="${STAGING_COMPOSE_PROJECT:-edrc-staging-smoke}"

cleanup() {
  docker compose -p "$project_name" -f "$compose_file" down --volumes --remove-orphans >/dev/null 2>&1 || true
}

trap cleanup EXIT

docker build --file "$root_dir/apps/api/Dockerfile" --tag edrc-land-gis-api:staging-smoke "$root_dir"
docker compose -p "$project_name" -f "$compose_file" up -d
API_URL="${API_URL:-http://localhost:8080}" "$root_dir/tools/operations/staging-smoke-test.sh"
