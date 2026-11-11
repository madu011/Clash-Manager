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


def test_api_documentation_is_not_publicly_exposed_yet() -> None:
    assert client.get("/docs").status_code == 404
    assert client.get("/openapi.json").status_code == 404


def test_security_headers_and_generated_request_id_are_present() -> None:
    response = client.get("/health")

    assert response.headers["X-Request-Id"]
    assert response.headers["X-Content-Type-Options"] == "nosniff"
    assert response.headers["X-Frame-Options"] == "DENY"
    assert response.headers["Referrer-Policy"] == "no-referrer"
    assert response.headers["Cache-Control"] == "no-store"


def test_safe_request_id_is_preserved() -> None:
    response = client.get("/health", headers={"X-Request-Id": "mobile-client:123"})

    assert response.headers["X-Request-Id"] == "mobile-client:123"


def test_invalid_request_id_is_replaced() -> None:
    response = client.get("/health", headers={"X-Request-Id": "bad value\r\nX-Evil: yes"})

    assert " " not in response.headers["X-Request-Id"]
    assert "X-Evil" not in response.headers["X-Request-Id"]
