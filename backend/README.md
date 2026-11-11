# Clash Manager Analysis Backend

## Current status

This directory contains an early, health-only backend foundation. It is not deployed and the Android app does not connect to it. Screenshot upload and AI analysis are intentionally disabled until authentication, explicit consent, server-side validation, retention controls, and an evaluated model are implemented.

## Runtime

- Python 3.12+
- FastAPI and Uvicorn
- Pinned dependencies in `requirements.txt`
- Container runs as an unprivileged user
- No API keys or provider credentials are required or stored here

## Local development

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Health check: `GET /health`.

## Container

From this directory:

```bash
docker build -t clash-manager-backend .
docker run --rm -p 8000:8000 clash-manager-backend
```

The container process listens on port 8000 and runs as a non-root user. A production deployment still needs a TLS-terminating ingress, authentication, resource limits, monitoring, and a documented privacy configuration.

## Current safeguards

- Interactive API docs and OpenAPI routes are disabled until the public API is ready.
- Responses include a generated or validated `X-Request-Id` for support correlation.
- Responses include baseline anti-sniffing, anti-framing, no-referrer, and no-store headers.
- The health response reveals no environment configuration or secrets.
- No upload or analysis route exists; `POST /v1/attack-analysis` returns 404.

## Security boundary

Do not add screenshot upload until short-lived app authentication, explicit per-analysis consent, server-side file signature and decode validation, request and rate limits, private temporary storage, retention cleanup, deletion, safe logging, and processor disclosure are implemented and tested. Never request Supercell ID credentials or put provider secrets in the Android app.

## Tests

Run `python -m pytest -q` from this directory. GitHub Actions runs these backend tests on changes to `backend/**`.
