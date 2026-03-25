from typing import Any

from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = Field(default="ok")


class ChartHint(BaseModel):
    type: str | None = None
    x: str | None = None
    y: str | None = None
    series: str | None = None


class ParamSpec(BaseModel):
    name: str
    type: str  # "date", "int", "float", "str"
    default: Any = None
    required: bool = True
    label: str = ""


# --------------- generate-sql ---------------

class GenerateSqlRequest(BaseModel):
    question: str


class GenerateSqlResponse(BaseModel):
    sqlTemplate: str
    paramsSpec: list[ParamSpec]
    reason: str
    chartHint: ChartHint
    confidence: float
    warnings: list[str]


# --------------- execute-sql (Python 端不直接用，但定义便于类型共享) ---------------

class ExecuteSqlRequest(BaseModel):
    sqlTemplate: str
    params: dict[str, Any]
    chartHint: ChartHint


class ExecuteSqlResponse(BaseModel):
    columns: list[str]
    rows: list[list[Any]]
    chartUrl: str | None = None
    sqlTemplate: str
    params: dict[str, Any]


# --------------- generate-chart ---------------

class GenerateChartRequest(BaseModel):
    question: str
    columns: list[str]
    rows: list[list[Any]]
    chartHint: ChartHint


class GenerateChartResponse(BaseModel):
    chartId: str
    fileName: str
