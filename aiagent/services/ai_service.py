import logging

from aiagent.api.schemas import (
    GenerateChartRequest,
    GenerateChartResponse,
    GenerateSqlRequest,
    GenerateSqlResponse,
)
from aiagent.agent.sql_agent import run_sql_agent
from aiagent.agent.chart_agent import build_chart_spec, select_chart_type
from aiagent.charts.plotter import render_chart
from aiagent.core.config import get_chart_output_dir
from aiagent.core.schema_loader import load_db_schema_structured, get_default_db_sql_path
from aiagent.core.schema_selector import render_selected_schema

logger = logging.getLogger(__name__)


def generate_sql_payload(payload: GenerateSqlRequest) -> GenerateSqlResponse:
    """调用 ReAct SQL Agent 生成参数化 SQL 模板（含自纠错循环）。

    Schema Linking: 根据用户问题动态选 5-8 张相关表注入 prompt（不再全量塞 29 张表），
    既缩短 prompt 又让 LLM 注意力聚焦在真正相关的表上。
    """
    all_tables = load_db_schema_structured(get_default_db_sql_path())
    schema_text = render_selected_schema(payload.question, all_tables)
    logger.info(
        "Schema 注入：选 %d 张表，文本 %d 字符（全量 %d 张表共 %d 字符）",
        schema_text.count("TABLE "),
        len(schema_text),
        len(all_tables),
        sum(len(t.render()) for t in all_tables.values()),
    )
    if not schema_text:
        logger.warning("db.sql schema 为空，LLM 无法生成有意义的 SQL")

    history_dicts = None
    if payload.history:
        history_dicts = [{"role": m.role, "content": m.content} for m in payload.history]

    sql_template, params_spec, reason, chart_hint, confidence, warnings, extra = run_sql_agent(
        question=payload.question,
        schema_text=schema_text,
        history=history_dicts,
    )

    if not sql_template:
        logger.warning("LLM 未能生成有效的 SQL 模板（question=%s）", payload.question)

    return GenerateSqlResponse(
        sqlTemplate=sql_template,
        paramsSpec=params_spec,
        reason=reason,
        chartHint=chart_hint,
        confidence=confidence,
        warnings=warnings,
        isVerified=extra.get("is_verified", False),
        retryCount=extra.get("retry_count", 0),
        sampleColumns=extra.get("sample_columns", []),
        sampleRows=extra.get("sample_rows", []),
    )


def generate_chart_artifact(payload: GenerateChartRequest) -> GenerateChartResponse:
    """根据查询结果生成图表 PNG 并返回文件信息。"""
    chart_id, file_name = render_chart(
        question=payload.question,
        columns=payload.columns,
        rows=payload.rows,
        chart_hint=payload.chartHint,
        output_dir=get_chart_output_dir(),
    )
    return GenerateChartResponse(chartId=chart_id, fileName=file_name)
