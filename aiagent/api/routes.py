import logging

from fastapi import APIRouter, FastAPI, HTTPException

from aiagent.api.schemas import (
    GenerateChartRequest,
    GenerateChartResponse,
    GenerateSqlRequest,
    GenerateSqlResponse,
    HealthResponse,
)
from aiagent.services.ai_service import generate_chart_artifact, generate_sql_payload

logger = logging.getLogger(__name__)

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
    try:
        return generate_chart_artifact(payload)
    except Exception as e:
        logger.error("图表生成失败: %s", e, exc_info=True)
        raise HTTPException(status_code=500, detail=f"图表生成失败: {e}")
