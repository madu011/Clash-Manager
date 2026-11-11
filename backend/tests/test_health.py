from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_reports_analysis_disabled() -> None:
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {
        "status": "ok",
        "service": "clash-manager-analysis",
        "analysis": "disabled",
    }


def test_unknown_routes_are_not_exposed() -> None:
    response = client.post("/v1/attack-analysis")

    assert response.status_code == 404
