# Deploying Castify (develop)

Push to `develop` → CI builds the `Dockerfile`, pushes the image to GHCR, and rolls
the Hetzner stack onto it. The server runs three containers defined by
`docker-compose.prod.yml` in this directory, which CI re-copies on every deploy so
the box can never drift from what is committed here.

```
internet ──▶ caddy :80/:443 ──▶ app :8080 ──▶ db :5432
                                  │            └─ volume: db-data
                                  └─ volumes: uploads, app-logs
```

Only Caddy publishes ports. The app and database are reachable solely on the compose
network.

## Files

| File | Role |
|---|---|
| `bootstrap-server.sh` | one-time server setup: Docker, `/opt/castify`, secrets, firewall, backup cron |
| `docker-compose.prod.yml` | the stack; copied to `/opt/castify/` on every deploy |
| `Caddyfile` | reverse proxy config; copied alongside it |
| `backup.sh` | nightly `pg_dump`, installed at `/opt/castify/backup.sh` |

Two env files live on the server and are **not** in git:

- `/opt/castify/.env` — secrets and settings, written once by the bootstrap script
- `/opt/castify/.env.image` — just `APP_IMAGE=...`, rewritten by CI each deploy

## Rebuilding the server from scratch

### 1. Create the server

Hetzner Cloud → Ubuntu 24.04, CX22 (2 vCPU / 4 GB) or larger. Add your personal SSH
key at creation. Note the public IPv4.

### 2. Create a deploy key for GitHub Actions

```bash
ssh-keygen -t ed25519 -C "github-actions-castify" -f ~/.ssh/castify_deploy -N ""
ssh-copy-id -i ~/.ssh/castify_deploy.pub root@<SERVER_IP>
```

The workflow logs in as `root`, so the key must be in root's `authorized_keys`.

### 3. Bootstrap

```bash
scp deploy/bootstrap-server.sh deploy/backup.sh root@<SERVER_IP>:/root/
ssh root@<SERVER_IP> 'ALLOWED_ORIGINS=https://your-frontend bash /root/bootstrap-server.sh'
```

Installs Docker, creates `/opt/castify`, generates `DB_PASSWORD` and `JWT_SECRET`
into `/opt/castify/.env`, installs the backup cron, and opens 22/80/443.

**Back up `/opt/castify/.env`.** It is generated, not in git, and not recoverable —
losing it means losing the database password.

### 4. Set the GitHub secrets

| Secret | Value |
|---|---|
| `HETZNER_SERVER_IP` | the new public IPv4 |
| `HETZNER_SSH_KEY` | contents of `~/.ssh/castify_deploy` (private key, incl. BEGIN/END lines) |

```bash
gh secret set HETZNER_SERVER_IP --body "<SERVER_IP>"
gh secret set HETZNER_SSH_KEY < ~/.ssh/castify_deploy
```

No registry credentials are needed on the server — the workflow logs Docker in with
the job's short-lived `GITHUB_TOKEN` and logs out afterwards.

### 5. Deploy

Push to `develop`, or run the workflow manually. The deploy blocks until
`/actuator/health` returns 200 through Caddy, and dumps container logs if it doesn't
within 150s.

Then: `http://<SERVER_IP>/swagger-ui.html`

## Day-to-day

Use the `dc` wrapper, never a bare `docker compose`. Compose auto-loads `.env`
but not `.env.image`, and it interpolates the entire file before running any
command — so `docker compose ... ps` fails with *"required variable APP_IMAGE is
missing a value"*. The wrapper passes both env files.

```bash
/opt/castify/dc ps
/opt/castify/dc logs -f app
/opt/castify/dc exec db psql -U castify -d castify
/opt/castify/dc restart app
/opt/castify/backup.sh                         # manual dump
```

**Rollback:** Actions → *Deploy Backend to Develop* → *Run workflow* → put a previous
tag such as `sha-1a2b3c4` in the `image_tag` input. It skips the build and redeploys
that image. Tags are visible under the repo's Packages.

**Restore a dump:**

```bash
gunzip -c /var/backups/castify/castify-YYYY-MM-DD_HHMM.sql.gz \
  | /opt/castify/dc exec -T db psql -U castify -d castify
```

## Adding HTTPS

Point a hostname's DNS at the server, then:

```bash
sed -i 's|^SITE_ADDRESS=.*|SITE_ADDRESS=api.example.com|' /opt/castify/.env
/opt/castify/dc up -d caddy
```

Caddy obtains and renews the certificate itself; no certbot. Remember to add the
HTTPS origin to `ALLOWED_ORIGINS` in the same file and restart `app`.

## Notes

- The stack runs the **`develop`** Spring profile, so Swagger UI is exposed. Switch
  `SPRING_PROFILES_ACTIVE` in `/opt/castify/.env` to `production` to turn it off —
  that profile also requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` and
  `ALLOWED_ORIGINS`, all of which compose already supplies.
- `application-develop.yml` writes its log to `/var/log/castify/`, which doesn't
  exist in the image and isn't writable by the non-root user. Compose overrides it
  via `LOGGING_FILE_NAME` to `/app/logs/castify.log` on a volume.
- Compose raises the multipart limit to 100MB. `application.yml` ships 10MB, which is
  below what the upload use cases themselves permit for portfolio video.
- Backups sit on the same disk as the database. They protect against bad migrations
  and accidental deletes, not against losing the server — copy `/var/backups/castify`
  off-box if that matters.
- Uploaded media lives in the `uploads` volume, not in Postgres, so `pg_dump` does
  **not** capture it. Back the volume up separately if the files matter.
