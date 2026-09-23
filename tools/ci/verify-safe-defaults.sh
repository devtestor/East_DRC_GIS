#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
application_file="$root_dir/apps/api/src/main/resources/application.yml"
security_file="$root_dir/apps/api/src/main/java/cd/edrc/landgis/config/SecurityConfig.java"

grep -Eq 'enabled: \$\{DEV_SEED_ENABLED:false\}' "$application_file"
grep -Eq 'max-file-size: \$\{MAX_FILE_SIZE:' "$application_file"
grep -Eq 'max-request-size: \$\{MAX_REQUEST_SIZE:' "$application_file"
grep -q 'contentSecurityPolicy' "$security_file"
grep -q 'frameOptions' "$security_file"
grep -q 'permissionsPolicy' "$security_file"

if rg -n --hidden --glob '!*.lock' --glob '!.git/**' \
  '(AKIA[0-9A-Z]{16}|-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----|ghp_[A-Za-z0-9]{30,})' "$root_dir"; then
  echo "Potential credential material found in the repository" >&2
  exit 1
fi

echo "Safe-default checks passed"
