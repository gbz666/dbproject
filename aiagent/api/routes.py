from fastapi import APIRouter, FastAPI

from aiagent.api.schemas import (
    GenerateChartRequest,
    GenerateChartResponse,
    GenerateSqlRequest,
    GenerateSqlResponse,
    HealthResponse,
)
from aiagent.services.ai_service import generate_chart_artifact, generate_sql_payload


router = APIRouter()


def register_routes(app: FastAPI) -> None:
    app.include_router(router)


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(status="ok")


@router.post("/generate-sql", response_model=GenerateSqlResponse)
def generate_sql_endpoint(payload: GenerateSqlRequest) -> GenerateSqlResponse:
    return generate_sql_payload(payload)


@router.post("/generate-chart", response_model=GenerateChartResponse)
def generate_chart_endpoint(payload: GenerateChartRequest) -> GenerateChartResponse:
    return generate_chart_artifact(payload)
