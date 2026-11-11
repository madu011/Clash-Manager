# Clash Manager Analysis Backend

## Current status

This directory is the starting point for a separate backend service. It is not deployed and the Android app does not connect to it. The first milestone is a minimal health-only service; screenshot upload and AI analysis remain disabled until authentication, consent, validation, retention, and a real evaluated model are implemented.

## Runtime

- Python 3.12+
- FastAPI and Uvicorn
- Environment-based configuration only
- No API keys or provider credentials in source control

## Local development

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Health check: `GET /health`.

## Security boundary

The initial service intentionally has no upload or analysis route. Do not add one until short-lived app authentication, explicit per-analysis consent, server-side file validation, rate limits, private temporary storage, retention cleanup, deletion, safe logging, and processor disclosure have tests. Never request Supercell ID credentials or put provider secrets in the Android app.
