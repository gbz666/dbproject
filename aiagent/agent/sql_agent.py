import logging

from aiagent.api.schemas import ChartHint, ParamSpec
from aiagent.core.llm_client import call_llm_json

logger = logging.getLogger(__name__)


def build_system_prompt(schema_text: str) -> str:
    """构建包含数据库 schema 上下文的系统提示词。"""
    return f"""你是 SQL 生成助手。根据用户问题和下方 Schema 生成 MySQL SELECT 查询。

Schema:
{schema_text}

规则:
- 只写 SELECT，禁止写操作
- 可变条件用 {{{{param_name}}}} 占位
- 大多数表有 is_deleted 字段，加 WHERE is_deleted = 0
- 金额字段是 DECIMAL，注意 SUM/AVG
- 注意外键 JOIN

你必须只输出一个 JSON 对象，不要输出任何其他文字。格式如下:
{{"sqlTemplate":"SELECT ... FROM ... WHERE ...","paramsSpec":[{{"name":"top_n","type":"int","default":"10","required":true,"label":"数量"}}],"reason":"说明","chartHint":{{"type":"bar","x":"列名","y":"列名","series":null}},"confidence":0.85,"warnings":[]}}

图表type选择: 时间趋势→line, 分类对比→bar, 占比且≤8类→pie, 其他→bar"""


def build_sql_prompt(question: str) -> str:
    """将用户自然语言问题转为 LLM 用户消息。"""
    return f"用户问题：{question}\n\n只输出 JSON，不要解释。"


def parse_llm_response(raw: dict) -> tuple[str, list[ParamSpec], str, ChartHint, float, list[str]]:
    """
    解析 LLM 结构化 JSON 输出为：
    (sqlTemplate, paramsSpec, reason, chartHint, confidence, warnings)
    """
    sql_template = raw.get("sqlTemplate", "")
    reason = raw.get("reason", "")
    confidence = float(raw.get("confidence", 0.0))
    warnings = raw.get("warnings", [])
    if isinstance(warnings, str):
        warnings = [warnings] if warnings else []

    params_raw = raw.get("paramsSpec", [])
    params_spec: list[ParamSpec] = []
    for p in params_raw:
        params_spec.append(ParamSpec(
            name=p.get("name", ""),
            type=p.get("type", "str"),
            default=p.get("default"),
            required=p.get("required", True),
            label=p.get("label", p.get("name", "")),
        ))

    hint_raw = raw.get("chartHint", {}) or {}
    chart_hint = ChartHint(
        type=hint_raw.get("type"),
        x=hint_raw.get("x"),
        y=hint_raw.get("y"),
        series=hint_raw.get("series"),
    )

    return sql_template, params_spec, reason, chart_hint, confidence, warnings


def run_sql_agent(
    question: str,
    schema_text: str,
) -> tuple[str, list[ParamSpec], str, ChartHint, float, list[str]]:
    """
    完整执行一次 NL->SQL 生成流程：
    1. 构建 prompt
    2. 调用 LLM
    3. 解析结构化输出
    返回 (sqlTemplate, paramsSpec, reason, chartHint, confidence, warnings)
    """
    system_msg = build_system_prompt(schema_text)
    user_msg = build_sql_prompt(question)

    messages = [
        {"role": "system", "content": system_msg},
        {"role": "user", "content": user_msg},
    ]

    logger.info("调用 LLM 生成 SQL，问题: %s", question)
    raw = call_llm_json(messages, temperature=0)
    logger.info("LLM 响应: %s", raw)

    return parse_llm_response(raw)
