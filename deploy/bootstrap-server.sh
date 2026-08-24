#!/usr/bin/env bash
# =============================================================================
# Castify - one-time bootstrap for a fresh Ubuntu 24.04 Hetzner server.
#
# The deploy workflow builds a container image, pushes it to GHCR, and then
# runs `docker compose pull && up -d` over SSH. All this script has to do is
# install Docker, create /opt/castify, and write the secrets the stack needs.
#
# Run once, as root, on the new server:
#   scp deploy/bootstrap-server.sh root@<SERVER_IP>:/root/
#   ssh root@<SERVER_IP> 'ALLOWED_ORIGINS=https://your-frontend bash /root/bootstrap-server.sh'
#
# Re-running is safe: /opt/castify/.env is never regenerated, so the database
# password and JWT secret survive.
# =============================================================================
set -euo pipefail

STACK_DIR=/opt/castify
ENV_FILE="${STACK_DIR}/.env"

ALLOWED_ORIGINS="${ALLOWED_ORIGINS:-http://localhost:5173}"
SITE_ADDRESS="${SITE_ADDRESS:-:80}"
ENABLE_UFW="${ENABLE_UFW:-yes}"

# ---------------------------------------------------------------------------
# 1. Docker Engine + Compose plugin (official apt repository)
# ---------------------------------------------------------------------------
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq ca-certificates curl gnupg ufw

if ! command -v docker >/dev/null; then
	install -m 0755 -d /etc/apt/keyrings
	curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
		-o /etc/apt/keyrings/docker.asc
	chmod a+r /etc/apt/keyrings/docker.asc
	. /etc/os-release
	echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" \
		> /etc/apt/sources.list.d/docker.list
	apt-get update -qq
	apt-get install -y -qq docker-ce docker-ce-cli containerd.io \
		docker-buildx-plugin docker-compose-plugin
fi
systemctl enable --now docker

# ---------------------------------------------------------------------------
# 2. Stack directory
# ---------------------------------------------------------------------------
install -d -m 750 "$STACK_DIR"
install -d -m 750 /var/backups/castify

# ---------------------------------------------------------------------------
# 3. Secrets (written once - regenerating JWT_SECRET logs every user out, and
#    regenerating DB_PASSWORD orphans the existing database volume)
# ---------------------------------------------------------------------------
if [[ ! -f "$ENV_FILE" ]]; then
	cat > "$ENV_FILE" <<EOF
# Long-lived configuration for the Castify stack.
# APP_IMAGE is NOT here - CI writes it to .env.image on every deploy.

SPRING_PROFILES_ACTIVE=develop

DB_NAME=castify
DB_USERNAME=castify
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '/+=')

JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
JWT_EXPIRATION_MS=86400000

# Comma-separated frontend origins. The browser blocks every call if the
# origin it loads from is not listed here.
ALLOWED_ORIGINS=${ALLOWED_ORIGINS}

# ":80" serves plain HTTP on the bare IP. Replace with a hostname (and point
# its DNS at this server) to have Caddy issue a TLS certificate automatically.
SITE_ADDRESS=${SITE_ADDRESS}
EOF
	chmod 600 "$ENV_FILE"
	echo ">> Wrote ${ENV_FILE} with generated secrets"
else
	echo ">> ${ENV_FILE} already exists, left untouched"
fi

# ---------------------------------------------------------------------------
# 4. Nightly database backup
# ---------------------------------------------------------------------------
[[ -f /root/dc ]] && install -m 750 /root/dc "${STACK_DIR}/dc"

if [[ -f /root/backup.sh ]]; then
	install -m 750 /root/backup.sh "${STACK_DIR}/backup.sh"
	cat > /etc/cron.d/castify-backup <<'EOF'
30 3 * * * root /opt/castify/backup.sh >> /var/log/castify-backup.log 2>&1
EOF
	chmod 644 /etc/cron.d/castify-backup
	echo ">> Installed nightly backup at 03:30"
else
	echo ">> WARNING: /root/backup.sh not found, skipping backup cron."
	echo "   scp deploy/backup.sh root@<ip>:/root/ and re-run to install it."
fi

# ---------------------------------------------------------------------------
# 5. Firewall - only SSH and the proxy ports. Postgres and the app are on the
#    compose network and are never published to the host.
# ---------------------------------------------------------------------------
if [[ "$ENABLE_UFW" == "yes" ]]; then
	ufw allow 22/tcp
	ufw allow 80/tcp
	ufw allow 443/tcp
	ufw --force enable
fi

echo
echo "=============================================================="
echo "Server is ready. Nothing is running yet - the first deploy"
echo "brings the stack up. Next:"
echo "  1. set the GitHub secrets HETZNER_SERVER_IP and HETZNER_SSH_KEY"
echo "  2. push to develop (or re-run the Deploy workflow)"
echo "  3. watch it:  cd /opt/castify && docker compose logs -f"
echo
echo "Generated secrets are in ${ENV_FILE} - back that file up somewhere,"
echo "it is not recoverable and not in git."
echo "=============================================================="
