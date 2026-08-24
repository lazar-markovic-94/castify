#!/usr/bin/env bash
# =============================================================================
# Nightly database dump. Installed at /opt/castify/backup.sh by
# bootstrap-server.sh and run from cron at 03:30.
#
# Keeps 14 days locally. The volume lives on the same disk as the database, so
# this protects against bad migrations and fat-fingered deletes, NOT against
# losing the server. Copy /var/backups/castify off-box for that.
# =============================================================================
set -euo pipefail

STACK_DIR=/opt/castify
BACKUP_DIR=/var/backups/castify
RETENTION_DAYS=14

# Both env files are needed: compose interpolates the whole file before running
# any command, and services.app.image requires APP_IMAGE even though this script
# only ever touches the db service.
# shellcheck disable=SC1091
set -a
. "${STACK_DIR}/.env"
[[ -f "${STACK_DIR}/.env.image" ]] && . "${STACK_DIR}/.env.image"
set +a
DB_NAME="${DB_NAME:-castify}"
DB_USERNAME="${DB_USERNAME:-castify}"

mkdir -p "$BACKUP_DIR"
STAMP="$(date +%Y-%m-%d_%H%M)"
TARGET="${BACKUP_DIR}/castify-${STAMP}.sql.gz"

cd "$STACK_DIR"
docker compose -f docker-compose.prod.yml exec -T db \
	pg_dump -U "$DB_USERNAME" -d "$DB_NAME" --clean --if-exists \
	| gzip > "$TARGET"

# A dump that is only a few hundred bytes means pg_dump failed but gzip
# succeeded - fail loudly rather than rotating a good backup out for a bad one.
if [[ "$(wc -c < "$TARGET")" -lt 1024 ]]; then
	echo "ERROR: ${TARGET} is suspiciously small, keeping it but not rotating." >&2
	exit 1
fi

find "$BACKUP_DIR" -name 'castify-*.sql.gz' -mtime "+${RETENTION_DAYS}" -delete
echo "Backup written: ${TARGET} ($(du -h "$TARGET" | cut -f1))"
