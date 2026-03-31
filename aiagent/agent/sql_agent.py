import logging
import textwrap

from aiagent.api.schemas import ChartHint, ParamSpec
from aiagent.core.llm_client import call_llm_json

logger = logging.getLogger(__name__)

_FEW_SHOT_EXAMPLES = textwrap.dedent("""\
--- 示例1 ---
问题: 查询本季度销售额前10的产品
输出:
{"sqlTemplate":"SELECT p.product_name, SUM(soi.quantity * soi.unit_price - soi.discount) AS total_sales FROM sales_order_items soi JOIN products p ON soi.product_id = p.id JOIN sales_orders so ON soi.sales_order_id = so.id WHERE so.is_deleted = 0 AND so.order_date >= {start_date} AND so.order_date < {end_date} GROUP BY p.product_name ORDER BY total_sales DESC LIMIT {top_n}","paramsSpec":[{"name":"start_date","type":"date","default":"2026-01-01","required":true,"label":"开始日期"},{"name":"end_date","type":"date","default":"2026-04-01","required":true,"label":"结束日期"},{"name":"top_n","type":"int","default":"10","required":false,"label":"前N名"}],"reason":"按产品分组聚合销售额并取前N","chartHint":{"type":"bar","x":"product_name","y":"total_sales","series":null},"confidence":0.85,"warnings":[]}

--- 示例2 ---
问题: 近6个月各月销售总额趋势
输出:
{"sqlTemplate":"SELECT DATE_FORMAT(so.order_date, '%Y-%m') AS month, SUM(so.total_amount) AS monthly_sales FROM sales_orders so WHERE so.is_deleted = 0 AND so.order_date >= {start_date} AND so.order_date < {end_date} GROUP BY month ORDER BY month","paramsSpec":[{"name":"start_date","type":"date","default":"2025-10-01","required":true,"label":"开始日期"},{"name":"end_date","type":"date","default":"2026-04-01","required":true,"label":"结束日期"}],"reason":"按月份聚合销售订单总额，展示趋势","chartHint":{"type":"line","x":"month","y":"monthly_sales","series":null},"confidence":0.88,"warnings":[]}

--- 示例3 ---
问题: 各产品分类的销售占比
输出:
{"sqlTemplate":"SELECT pc.category_name, SUM(soi.quantity * soi.unit_price - soi.discount) AS total_sales FROM sales_order_items soi JOIN products p ON soi.product_id = p.id JOIN product_categories pc ON p.category_id = pc.id JOIN sales_orders so ON soi.sales_order_id = so.id WHERE so.is_deleted = 0 AND p.is_deleted = 0 AND so.order_date >= {start_date} AND so.order_date < {end_date} GROUP BY pc.category_name ORDER BY total_sales DESC","paramsSpec":[{"name":"start_date","type":"date","default":"2026-01-01","required":true,"label":"开始日期"},{"name":"end_date","type":"date","default":"2026-04-01","required":true,"label":"结束日期"}],"reason":"按产品分类聚合销售额，适合饼图展示占比","chartHint":{"type":"pie","x":"category_name","y":"total_sales","series":null},"confidence":0.82,"warnings":[]}
""")


def build_system_prompt(schema_text: str) -> str:
    """构建包含数据库 schema 上下文、few-shot 示例和严格输出约束的系统提示词。"""
    return textwrap.dedent(f"""\
你是 MySQL 查询生成助手。根据用户的自然语言问题和下方 Schema 生成参数化 SELECT 查询。

=== 数据库 Schema ===
{schema_text}

=== 核心规则 ===
1. 只生成 SELECT / WITH 查询，禁止任何写操作
2. 可变条件用 {{param_name}} 占位（如 {{start_date}}、{{top_n}}），不要硬编码
3. 大多数表有 is_deleted 字段，查询时加 WHERE is_deleted = 0
4. 金额字段为 DECIMAL，使用 SUM/AVG 时注意精度
5. 关联表时使用 JOIN，注意正确的外键关系
6. 为每个占位参数提供 paramsSpec，包含 name/type/default/required/label
7. chartHint.type 选择规则: 时间序列趋势→line, 分类对比→bar, 占比且分类≤8→pie, 其他→bar

=== 输出格式（严格遵守）===
你**必须且只能**输出一个 JSON 对象，禁止输出 markdown 代码块、解释文字或任何非 JSON 内容。
JSON 必须包含以下全部字段:
- sqlTemplate: string（参数化 SQL）
- paramsSpec: array（参数规格列表）
- reason: string（生成理由，一句话说明）
- chartHint: object（含 type/x/y/series 四个字段，series 可为 null）
- confidence: number（0~1 之间的置信度）
- warnings: array（空数组或注意事项列表）

=== 参考示例 ===
{_FEW_SHOT_EXAMPLES}
以上示例仅作格式参考，你需要根据实际问题和 Schema 生成正确的 SQL。""")


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
    history: list[dict] | None = None,
) -> tuple[str, list[ParamSpec], str, ChartHint, float, list[str]]:
    """
    完整执行一次 NL->SQL 生成流程：
    1. 构建 prompt
    2. 如有 history 则加入对话上下文
    3. 调用 LLM
    4. 解析结构化输出
    返回 (sqlTemplate, paramsSpec, reason, chartHint, confidence, warnings)
    """
    system_msg = build_system_prompt(schema_text)

    messages: list[dict] = [{"role": "system", "content": system_msg}]

    if history:
        for msg in history:
            messages.append({"role": msg["role"], "content": msg["content"]})

    user_msg = build_sql_prompt(question)
    messages.append({"role": "user", "content": user_msg})

    logger.info("调用 LLM 生成 SQL，问题: %s, 历史轮数: %d", question, len(history or []))
    raw = call_llm_json(messages, temperature=0)
    logger.info("LLM 响应: %s", raw)

    return parse_llm_response(raw)
