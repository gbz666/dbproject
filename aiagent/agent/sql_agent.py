import logging
import textwrap

from aiagent.api.schemas import ChartHint, ParamSpec
from aiagent.core.llm_client import call_llm_json

logger = logging.getLogger(__name__)

_FEW_SHOT_EXAMPLES = textwrap.dedent("""\
--- 示例1 ---
问题: 查询本季度销售额前10的产品
输出:
{"sqlTemplate":"SELECT p.product_name, SUM(soi.quantity * (soi.unit_price - soi.cost_price)) AS total_sales FROM sales_order_items soi JOIN products p ON soi.product_id = p.id JOIN sales_orders so ON soi.sales_order_id = so.id WHERE so.is_deleted = 0 AND so.order_date >= {start_date} AND so.order_date < {end_date} GROUP BY p.product_name ORDER BY total_sales DESC LIMIT {top_n}","paramsSpec":[{"name":"start_date","type":"date","default":"2026-01-01","required":true,"label":"开始日期"},{"name":"end_date","type":"date","default":"2026-04-01","required":true,"label":"结束日期"},{"name":"top_n","type":"int","default":"10","required":false,"label":"前N名"}],"reason":"按产品分组聚合销售额并取前N","chartHint":{"type":"bar","x":"product_name","y":"total_sales","series":null},"confidence":0.85,"warnings":[]}

--- 示例2 ---
问题: 查询2025年各月纯利润总额趋势
输出:
{
  "sqlTemplate": "SELECT 
    DATE_FORMAT(so.order_date, '%Y-%m') AS month, 
    -- 使用 IFNULL 确保哪怕字段是 NULL 也能当作 0 计算
    SUM(
        (IFNULL(soi.unit_price, 0) * IFNULL(soi.quantity, 0)) - 
        (IFNULL(soi.cost_price, 0) * IFNULL(soi.quantity, 0)) - 
        IFNULL(soi.discount, 0)
    ) AS monthly_profit 
FROM sales_order_items soi 
JOIN sales_orders so ON soi.sales_order_id = so.id 
WHERE so.is_deleted = 0 
  AND so.order_date >= '2025-01-01' 
  AND so.order_date < '2026-01-01' 
GROUP BY month 
ORDER BY month;",
  "paramsSpec": [
    {
      "name": "start_date",
      "type": "date",
      "default": "2025-01-01",
      "required": true,
      "label": "开始日期"
    },
    {
      "name": "end_date",
      "type": "date",
      "default": "2026-01-01",
      "required": true,
      "label": "结束日期"
    }
  ],
  "reason": "按月份聚合销售利润（销售总额 - 商品成本总额），展示年度盈利变化趋势",
  "chartHint": {
    "type": "line",
    "x": "month",
    "y": "monthly_profit",
    "series": null
  },
  "confidence": 0.95,
  "warnings": [
    "确保 sales_order_items 表中已包含有效的 cost_price 数据",
    "利润计算结果已自动处理 NULL 值"
  ]
}

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
8. 涉及到数值计算，必须给可能为空的字段设置默认值（通常是 0）
9. **严格禁止使用 Schema 中不存在的表名或列名**。如果不确定某张表是否存在，不要猜测，只使用上方 Schema 中列出的表和列。如果问题涉及的表在 Schema 中找不到，请在 warnings 中说明，并尽量用已有的表生成最接近的查询
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


def build_correction_prompt(question: str, failed_sql: str, error_msg: str) -> str:
    """
    构建带错误反馈的修正 prompt，让 LLM 反思并修正 SQL。
    用于 ReAct 循环中试跑失败后的重试。
    如果 failed_sql 为空，改为让 LLM 重新生成。
    """
    if not failed_sql or not failed_sql.strip():
        return textwrap.dedent(f"""\
用户问题：{question}

你上一次未能生成有效的 SQL 查询。请重新分析用户问题，结合数据库 Schema 生成正确的参数化 SQL。

注意：
- 确保输出完整的 SELECT 语句
- 如果问题不够明确，根据 Schema 做合理假设
- 不要输出空的 sqlTemplate

只输出 JSON，不要解释。""")

    return textwrap.dedent(f"""\
用户问题：{question}

你上一次生成的 SQL 在数据库试跑时失败了：

=== 失败的 SQL ===
{failed_sql}

=== 数据库报错 ===
{error_msg}

请分析错误原因，修正 SQL 后重新输出。常见错误包括：
- 列名或表名拼写错误
- JOIN 关系不正确（检查外键列名）
- 缺少 GROUP BY 中的非聚合列
- 数据类型不匹配

只输出修正后的 JSON，不要解释。""")


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

    # LLM 有时会返回 list 而非 string（如对比查询时 y 可能为 ['col1', 'col2']）
    def _coerce_str(val) -> str | None:
        if val is None:
            return None
        if isinstance(val, list):
            return val[0] if val else None
        return str(val)

    chart_hint = ChartHint(
        type=_coerce_str(hint_raw.get("type")),
        x=_coerce_str(hint_raw.get("x")),
        y=_coerce_str(hint_raw.get("y")),
        series=_coerce_str(hint_raw.get("series")),
    )

    return sql_template, params_spec, reason, chart_hint, confidence, warnings


def run_sql_agent(
    question: str,
    schema_text: str,
    history: list[dict] | None = None,
) -> tuple[str, list[ParamSpec], str, ChartHint, float, list[str], dict]:
    """
    使用 LangGraph ReAct Agent 执行 NL->SQL 生成流程（含自纠错循环）。

    流程：生成 SQL → 试跑 → 失败则带错误重试 → 直到成功或达到最大重试次数

    返回:
        (sqlTemplate, paramsSpec, reason, chartHint, confidence, warnings, extra)
        extra 包含: is_verified, retry_count, sample_columns, sample_rows
    """
    from aiagent.agent.react_sql_agent import run_react_sql_agent

    logger.info("启动 ReAct SQL Agent，问题: %s, 历史轮数: %d", question, len(history or []))
    result = run_react_sql_agent(question, schema_text, history)

    # 解析 params_spec 从 dict 列表回到 ParamSpec 对象
    params_spec = []
    for p in result.get("params_spec", []):
        if isinstance(p, dict):
            params_spec.append(ParamSpec(
                name=p.get("name", ""),
                type=p.get("type", "str"),
                default=p.get("default"),
                required=p.get("required", True),
                label=p.get("label", p.get("name", "")),
            ))
        else:
            params_spec.append(p)

    # 解析 chart_hint
    hint_raw = result.get("chart_hint", {}) or {}
    if isinstance(hint_raw, dict):
        chart_hint = ChartHint(
            type=hint_raw.get("type"),
            x=hint_raw.get("x"),
            y=hint_raw.get("y"),
            series=hint_raw.get("series"),
        )
    else:
        chart_hint = hint_raw

    extra = {
        "is_verified": result.get("is_verified", False),
        "retry_count": result.get("retry_count", 0),
        "sample_columns": result.get("sample_columns", []),
        "sample_rows": result.get("sample_rows", []),
    }

    return (
        result.get("sql_template", ""),
        params_spec,
        result.get("reason", ""),
        chart_hint,
        result.get("confidence", 0.0),
        result.get("warnings", []),
        extra,
    )
