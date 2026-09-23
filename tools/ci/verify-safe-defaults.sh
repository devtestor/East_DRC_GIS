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

secret_pattern='AKIA[0-9A-Z]{16}|-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----|ghp_[A-Za-z0-9]{30,}'
if command -v rg >/dev/null 2>&1; then
  secret_matches="$(rg -n --hidden --glob '!*.lock' --glob '!.git/**' "$secret_pattern" "$root_dir" || true)"
else
  secret_matches="$(grep -RInE --exclude='*.lock' --exclude-dir='.git' "$secret_pattern" "$root_dir" || true)"
fi

if [ -n "$secret_matches" ]; then
  printf '%s\n' "$secret_matches"
  echo "Potential credential material found in the repository" >&2
  exit 1
fi

echo "Safe-default checks passed"
