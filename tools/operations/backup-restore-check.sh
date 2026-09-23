#!/usr/bin/env bash
set -euo pipefail

: "${DB_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${DB_NAME:=edrc_land_gis}"
: "${DB_USER:=edrc_app}"
: "${DB_PASSWORD:?Set DB_PASSWORD for a restore rehearsal}"

work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT
dump_file="$work_dir/edrc-land-gis.dump"

echo "Creating isolated logical backup for ${DB_NAME}@${DB_HOST}:${DB_PORT}"
PGPASSWORD="$DB_PASSWORD" pg_dump \
  --format=custom \
  --no-owner \
  --file="$dump_file" \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --username="$DB_USER" \
  "$DB_NAME"

test -s "$dump_file"
echo "Backup created: $(du -h "$dump_file" | awk '{print $1}')"
echo "Restore rehearsal requires an isolated database and explicit operator approval."
echo "This script intentionally does not drop, create, or overwrite databases."
