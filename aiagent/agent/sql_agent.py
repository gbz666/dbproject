from aiagent.api.schemas import ChartHint, ParamSpec


def build_system_prompt(schema_text: str) -> str:
    """构建包含数据库 schema 上下文的系统提示词。"""
    pass


def build_sql_prompt(question: str) -> str:
    """将用户自然语言问题转为 LLM 用户消息。"""
    pass


def parse_llm_response(raw: dict) -> tuple[str, list[ParamSpec], str, ChartHint, float, list[str]]:
    """
    解析 LLM 结构化 JSON 输出为：
    (sqlTemplate, paramsSpec, reason, chartHint, confidence, warnings)
    """
    pass


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
    pass
