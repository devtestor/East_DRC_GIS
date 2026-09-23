#!/usr/bin/env bash
set -euo pipefail

: "${DB_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${DB_NAME:=edrc_land_gis}"
: "${DB_USER:=edrc_app}"
: "${DB_PASSWORD:?Set DB_PASSWORD for a restore rehearsal}"
: "${BACKUP_LABEL:=manual-restore-rehearsal}"

work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT
dump_file="$work_dir/edrc-land-gis.dump"
metadata_file="$work_dir/backup-metadata.txt"

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
pg_restore --list "$dump_file" >/dev/null

{
  echo "label=${BACKUP_LABEL}"
  echo "database=${DB_NAME}"
  echo "host=${DB_HOST}"
  echo "port=${DB_PORT}"
  echo "created_utc=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "sha256=$(sha256sum "$dump_file" | awk '{print $1}')"
} > "$metadata_file"

echo "Backup created: $(du -h "$dump_file" | awk '{print $1}')"
echo "Backup metadata:"
cat "$metadata_file"
echo "Restore rehearsal requires an isolated database and explicit operator approval."
echo "This script intentionally does not drop, create, or overwrite databases."
