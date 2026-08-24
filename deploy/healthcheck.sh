#!/usr/bin/env bash
# =============================================================================
# Post-deploy verification. Installed at /opt/castify/healthcheck.sh and run by
# the deploy workflow over SSH.
#
# Two probes, because either alone gives a misleading answer:
#   app  - /actuator/health from inside the compose network. Proves the
#          application started and reached the database.
#   edge - the same endpoint through Caddy. Proves TLS terminates and the
#          proxy can route to the app.
#
# Env: ATTEMPTS (default 30), INTERVAL seconds (default 5).
# =============================================================================
set -uo pipefail

STACK_DIR=/opt/castify
SITE=$(grep '^SITE_ADDRESS=' "${STACK_DIR}/.env" 2>/dev/null | cut -d= -f2- || true)

app_probe() {
	# </dev/null is required: `compose exec -T` reads stdin, and without it the
	# calling shell's script body gets consumed and execution stops silently.
	(cd "$STACK_DIR" && ./dc exec -T app \
		curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health </dev/null)
}

edge_probe() {
	if [[ -z "$SITE" || "$SITE" == :* ]]; then
		# Port-only site address: Caddy serves plain HTTP, no redirect.
		curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1/actuator/health
	else
		# A named site redirects HTTP to HTTPS, so probe HTTPS directly.
		# --resolve pins it to the local Caddy: following the redirect out to
		# the server's own public IP fails on hairpin routing, which is what
		# made an earlier version of this check fail a healthy deployment.
		curl -s --resolve "${SITE}:443:127.0.0.1" \
			-o /dev/null -w '%{http_code}' "https://${SITE}/actuator/health"
	fi
}

for i in $(seq 1 "${ATTEMPTS:-30}"); do
	APP=$(app_probe 2>/dev/null || echo 000)
	EDGE=$(edge_probe 2>/dev/null || echo 000)

	if [[ "$APP" == "200" && "$EDGE" == "200" ]]; then
		echo "Healthy after ${i} attempt(s) (app=${APP} edge=${EDGE}, site=${SITE:-:80})"
		exit 0
	fi
	echo "attempt ${i}: app=${APP} edge=${EDGE}"
	sleep "${INTERVAL:-5}"
done

echo "ERROR: stack did not become healthy in time" >&2
# Through ./dc - a bare `docker compose` cannot interpolate APP_IMAGE and dies
# before printing anything, which is how an earlier failure hid its own cause.
(cd "$STACK_DIR" && ./dc ps && ./dc logs --tail=100 app) || true
exit 1
