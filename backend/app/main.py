from fastapi import FastAPI

app = FastAPI(
    title="Clash Manager Analysis Service",
    version="0.1.0",
    description=(
        "Health-only foundation. Screenshot upload and AI analysis are not enabled."
    ),
)


@app.get("/health", tags=["operations"])
async def health() -> dict[str, str]:
    """Return process health without exposing configuration or secrets."""
    return {"status": "ok", "service": "clash-manager-analysis", "analysis": "disabled"}
