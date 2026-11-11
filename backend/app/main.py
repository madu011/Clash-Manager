import re
from uuid import uuid4

from fastapi import FastAPI, Request

app = FastAPI(
    title="Clash Manager Analysis Service",
    version="0.1.0",
    description=(
        "Health-only foundation. Screenshot upload and AI analysis are not enabled."
    ),
    docs_url=None,
    redoc_url=None,
    openapi_url=None,
)

_REQUEST_ID_PATTERN = re.compile(r"^[A-Za-z0-9._:-]{1,80}$")


@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    """Attach a safe correlation ID and baseline response security headers."""
    supplied_id = request.headers.get("X-Request-Id", "")
    request_id = supplied_id if _REQUEST_ID_PATTERN.fullmatch(supplied_id) else str(uuid4())
    request.state.request_id = request_id

    response = await call_next(request)
    response.headers["X-Request-Id"] = request_id
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Referrer-Policy"] = "no-referrer"
    response.headers["Cache-Control"] = "no-store"
    response.headers["Content-Security-Policy"] = "default-src 'none'; frame-ancestors 'none'"
    return response


@app.get("/health", tags=["operations"])
async def health() -> dict[str, str]:
    """Return process health without exposing configuration or secrets."""
    return {"status": "ok", "service": "clash-manager-analysis", "analysis": "disabled"}
