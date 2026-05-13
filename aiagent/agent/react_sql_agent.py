"""
LangGraph ReAct SQL Agent —— "生成 → 试跑 → 检查 → 修正" 自纠错循环。

流程图:
    START → generate_sql → execute_sql → check_result ─┐
                ↑                                       │
                └── (error & retries < MAX) ────────────┘
                                                        │
                                          (success or max retries)
                                                        ↓
                                                    finalize → END
"""
import logging
from typing import Any, TypedDict

from langgraph.graph import StateGraph, START, END

from aiagent.agent.sql_agent import (
    build_system_prompt,
    build_sql_prompt,
    build_correction_prompt,
    parse_llm_response,
)
from aiagent.core.llm_client import call_llm_json
from aiagent.core.db_executor import execute_readonly_sql

logger = logging.getLogger(__name__)

# 最大重试次数（不含首次生成，即最多 MAX_RETRIES + 1 次 LLM 调用）
MAX_RETRIES = 5


# ─── State 定义 ───────────────────────────────────────────────────────────────

class SqlAgentState(TypedDict, total=False):
    # 输入
    question: str
    schema_text: str
    history: list[dict]
    # LLM 输出
    sql_template: str
    params_spec: list[dict]
    reason: str
    chart_hint: dict
    confidence: float
    warnings: list[str]
    # 自纠错循环状态
    error_history: list[str]  # 历次试跑错误记录
    retry_count: int
    consecutive_empty: int   # 连续生成空 SQL 的次数
    # 试跑结果
    sample_columns: list[str]
    sample_rows: list[list]
    is_verified: bool


# ─── 辅助函数 ─────────────────────────────────────────────────────────────────

def _render_with_defaults(sql_template: str, params_spec: list[dict]) -> str:
    """将 SQL 模板中的 {param} 占位符替换为 paramsSpec 中的默认值，用于试跑。"""
    result = sql_template
    for p in params_spec:
        placeholder = "{" + p.get("name", "") + "}"
        default = p.get("default")
        if default is None:
            continue
        p_type = p.get("type", "str")
        if p_type in ("int", "float"):
            replacement = str(default)
        else:
            escaped = str(default).replace("'", "''")
            replacement = f"'{escaped}'"
        result = result.replace(placeholder, replacement)
    return result


# ─── 节点函数 ─────────────────────────────────────────────────────────────────

def generate_sql_node(state: SqlAgentState) -> dict:
    """调用 LLM 生成（或修正）SQL。"""
    schema_text = state.get("schema_text", "")
    question = state.get("question", "")
    history = state.get("history") or []
    error_history = state.get("error_history") or []
    retry_count = state.get("retry_count", 0)

    system_msg = build_system_prompt(schema_text)
    messages: list[dict] = [{"role": "system", "content": system_msg}]

    # 加入对话历史
    for msg in history:
        messages.append({"role": msg["role"], "content": msg["content"]})

    if not error_history:
        # 首次生成
        user_msg = build_sql_prompt(question)
        messages.append({"role": "user", "content": user_msg})
        logger.info("[ReAct] 首次生成 SQL，问题: %s", question)
    else:
        # 修正模式：将上次失败的 SQL 和错误信息告诉 LLM
        last_sql = state.get("sql_template", "")
        last_error = error_history[-1]
        correction_msg = build_correction_prompt(question, last_sql, last_error)
        messages.append({"role": "user", "content": correction_msg})
        logger.info(
            "[ReAct] 第 %d 次修正，上次错误: %s",
            retry_count, last_error[:200],
        )

    raw = call_llm_json(messages, temperature=0)
    sql_template, params_spec, reason, chart_hint, confidence, warnings = parse_llm_response(raw)

    logger.info("[ReAct] LLM 输出 SQL: %s", sql_template[:200] if sql_template else "(空)")

    return {
        "sql_template": sql_template,
        "params_spec": [p.model_dump() for p in params_spec],
        "reason": reason,
        "chart_hint": chart_hint.model_dump() if hasattr(chart_hint, "model_dump") else chart_hint,
        "confidence": confidence,
        "warnings": warnings,
    }


def execute_sql_node(state: SqlAgentState) -> dict:
    """用只读账号试跑 SQL，验证是否可执行。"""
    sql_template = state.get("sql_template", "")
    params_spec = state.get("params_spec") or []
    error_history = state.get("error_history") or []
    prev_empty = state.get("consecutive_empty", 0)

    if not sql_template:
        logger.warning("[ReAct] SQL 模板为空 (连续第 %d 次)", prev_empty + 1)
        return {
            "is_verified": False,
            "error_history": error_history + ["SQL 模板为空"],
            "retry_count": (state.get("retry_count", 0)) + 1,
            "consecutive_empty": prev_empty + 1,
            "sample_columns": [],
            "sample_rows": [],
        }

    # 用默认参数渲染 SQL
    rendered_sql = _render_with_defaults(sql_template, params_spec)
    logger.info("[ReAct] 试跑 SQL: %s", rendered_sql[:300])

    result = execute_readonly_sql(rendered_sql)

    if result["success"]:
        logger.info(
            "[ReAct] 试跑成功 ✓ 返回 %d 列, %d 行",
            len(result["columns"]), len(result["rows"]),
        )
        return {
            "is_verified": True,
            "consecutive_empty": 0,
            "sample_columns": result["columns"],
            "sample_rows": result["rows"],
        }
    else:
        error_msg = result["error"]
        logger.warning("[ReAct] 试跑失败 ✗ 错误: %s", error_msg[:300])
        return {
            "is_verified": False,
            "consecutive_empty": 0,  # SQL 非空，重置连续空计数
            "error_history": (state.get("error_history") or []) + [error_msg],
            "retry_count": (state.get("retry_count", 0)) + 1,
            "sample_columns": [],
            "sample_rows": [],
        }


def finalize_node(state: SqlAgentState) -> dict:
    """收尾节点：如果未通过验证，在 warnings 中附加说明。"""
    is_verified = state.get("is_verified", False)
    warnings = list(state.get("warnings") or [])
    retry_count = state.get("retry_count", 0)
    sql_template = state.get("sql_template", "")

    if not is_verified:
        error_history = state.get("error_history") or []
        if not sql_template:
            # LLM 始终未能生成 SQL
            warnings.append(
                "AI 未能理解您的问题，请尝试重新描述。\n"
                "示例：\n"
                "- 查询本季度销售额前10的产品\n"
                "- 查看2025年各月利润趋势\n"
                "- 统计各产品分类的销售占比"
            )
        else:
            # 有 SQL 但验证失败
            last_err = error_history[-1] if error_history else "未知错误"
            warnings.append(
                f"自动生成的 SQL 存在问题（重试 {retry_count} 次仍失败），已停止重试。\n"
                f"错误: {last_err[:200]}\n\n"
                "您可以：\n"
                "1. 点击「使用此 SQL」查看并手动修正\n"
                "2. 换一种方式描述您的查询需求，重新提问"
            )

    if retry_count > 0:
        logger.info("[ReAct] 最终状态: verified=%s, retries=%d, sql=%s", is_verified, retry_count, (sql_template or "")[:80])

    return {"warnings": warnings}


# ─── 条件路由 ─────────────────────────────────────────────────────────────────

# 连续空 SQL 达到此次数则停止重试（避免无意义循环）
MAX_CONSECUTIVE_EMPTY = 2


def should_retry(state: SqlAgentState) -> str:
    """决定是重试 generate_sql 还是进入 finalize。"""
    if state.get("is_verified", False):
        return "finalize"
    # 连续生成空 SQL 多次，说明 LLM 无法理解问题，停止重试
    if state.get("consecutive_empty", 0) >= MAX_CONSECUTIVE_EMPTY:
        return "finalize"
    if state.get("retry_count", 0) >= MAX_RETRIES:
        return "finalize"
    return "generate_sql"


# ─── 构建 Graph ───────────────────────────────────────────────────────────────

def _build_graph() -> StateGraph:
    """构建 LangGraph StateGraph 并编译。"""
    builder = StateGraph(SqlAgentState)

    builder.add_node("generate_sql", generate_sql_node)
    builder.add_node("execute_sql", execute_sql_node)
    builder.add_node("finalize", finalize_node)

    # 边
    builder.add_edge(START, "generate_sql")
    builder.add_edge("generate_sql", "execute_sql")
    builder.add_conditional_edges("execute_sql", should_retry)
    builder.add_edge("finalize", END)

    return builder.compile()


# 编译好的 graph 单例
_compiled_graph = None


def get_graph():
    global _compiled_graph
    if _compiled_graph is None:
        _compiled_graph = _build_graph()
    return _compiled_graph


# ─── 对外入口 ─────────────────────────────────────────────────────────────────

def run_react_sql_agent(
    question: str,
    schema_text: str,
    history: list[dict] | None = None,
) -> dict[str, Any]:
    """
    运行 LangGraph ReAct SQL Agent。

    返回:
        {
            "sql_template": str,
            "params_spec": list[dict],
            "reason": str,
            "chart_hint": dict,
            "confidence": float,
            "warnings": list[str],
            "is_verified": bool,
            "retry_count": int,
            "sample_columns": list[str],
            "sample_rows": list[list],
        }
    """
    graph = get_graph()

    initial_state: SqlAgentState = {
        "question": question,
        "schema_text": schema_text,
        "history": history or [],
        "sql_template": "",
        "params_spec": [],
        "reason": "",
        "chart_hint": {},
        "confidence": 0.0,
        "warnings": [],
        "error_history": [],
        "retry_count": 0,
        "consecutive_empty": 0,
        "sample_columns": [],
        "sample_rows": [],
        "is_verified": False,
    }

    logger.info("[ReAct] 启动 Agent，问题: %s", question)
    final_state = graph.invoke(initial_state)
    logger.info(
        "[ReAct] Agent 完成: verified=%s, retries=%d, sql=%s",
        final_state.get("is_verified"),
        final_state.get("retry_count", 0),
        (final_state.get("sql_template") or "")[:100],
    )

    return {
        "sql_template": final_state.get("sql_template", ""),
        "params_spec": final_state.get("params_spec", []),
        "reason": final_state.get("reason", ""),
        "chart_hint": final_state.get("chart_hint", {}),
        "confidence": final_state.get("confidence", 0.0),
        "warnings": final_state.get("warnings", []),
        "is_verified": final_state.get("is_verified", False),
        "retry_count": final_state.get("retry_count", 0),
        "sample_columns": final_state.get("sample_columns", []),
        "sample_rows": final_state.get("sample_rows", []),
    }
