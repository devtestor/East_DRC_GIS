#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

test -s "$root_dir/docs/architecture/phase-12-plan.md"
test -s "$root_dir/docs/operations/backup-recovery-runbook.md"
test -s "$root_dir/platform/infrastructure/recovery/backup-policy.md"
test -s "$root_dir/platform/infrastructure/recovery/restore-validation-checklist.md"
test -s "$root_dir/tools/operations/backup-restore-check.sh"

grep -qi 'point-in-time recovery' "$root_dir/docs/operations/backup-recovery-runbook.md"
grep -qi 'isolated recovery environment' "$root_dir/docs/operations/backup-recovery-runbook.md"
grep -qi 'immutable' "$root_dir/platform/infrastructure/recovery/backup-policy.md"
grep -qi 'RPO' "$root_dir/platform/infrastructure/recovery/backup-policy.md"
grep -qi 'RTO' "$root_dir/platform/infrastructure/recovery/backup-policy.md"
grep -qi 'PostGIS' "$root_dir/platform/infrastructure/recovery/restore-validation-checklist.md"
grep -qi 'payment callbacks' "$root_dir/platform/infrastructure/recovery/restore-validation-checklist.md"
grep -qi 'workflow workers are disabled' "$root_dir/platform/infrastructure/recovery/restore-validation-checklist.md"
grep -qi 'This script intentionally does not drop, create, or overwrite databases' "$root_dir/tools/operations/backup-restore-check.sh"

if grep -RInE 'dropdb|DROP DATABASE|createdb|CREATE DATABASE|psql .*--command=.*DROP|psql .*--command=.*CREATE DATABASE|rm -rf /' "$root_dir/tools/operations/backup-restore-check.sh"; then
  echo "Backup restore check script must stay non-destructive by default." >&2
  exit 1
fi

bash -n "$root_dir/tools/operations/backup-restore-check.sh"

echo "Backup and recovery checks passed"
