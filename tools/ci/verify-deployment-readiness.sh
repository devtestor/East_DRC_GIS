#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

test -s "$root_dir/docs/architecture/phase-9-plan.md"
test -s "$root_dir/docs/operations/staging-deployment-runbook.md"
test -s "$root_dir/platform/infrastructure/env/staging.env.example"
test -s "$root_dir/platform/infrastructure/compose/staging-compose.yml"
test -s "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
test -s "$root_dir/platform/infrastructure/kubernetes/api-secrets.example.yaml"
test -s "$root_dir/platform/infrastructure/terraform/main.tf"

grep -q 'DEV_SEED_ENABLED=false' "$root_dir/platform/infrastructure/env/staging.env.example"
grep -q 'DEV_SEED_ENABLED' "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
grep -q 'value: "false"' "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
grep -q 'secretKeyRef' "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
grep -q 'readinessProbe' "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
grep -q 'livenessProbe' "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
grep -q 'runAsNonRoot: true' "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml"
grep -q 'development_seed_disabled' "$root_dir/platform/infrastructure/terraform/main.tf"

if rg -n '__SET_IN_SECRET_MANAGER__|change-me-staging-smoke-only' \
  "$root_dir/platform/infrastructure/kubernetes/api-deployment.yaml" \
  "$root_dir/platform/infrastructure/kubernetes/api-service.yaml" \
  "$root_dir/docs"; then
  echo "Placeholder secrets must stay out of Kubernetes/docs deployment manifests except example files." >&2
  exit 1
fi

echo "Deployment readiness checks passed"
