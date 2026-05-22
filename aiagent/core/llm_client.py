import json
import logging
import os
import re

from openai import OpenAI

from aiagent.core.config import load_settings

logger = logging.getLogger(__name__)

# httpx（openai SDK 底层 HTTP 库）会读取系统代理，导致本地 Ollama 请求被
# Clash 等代理劫持返回 502。httpx.Client(proxy=None) 在 Windows 上无效——
# 它仍会从注册表读 WinHTTP 代理，所以必须通过环境变量强制绕过。
os.environ.setdefault("NO_PROXY", "127.0.0.1,localhost")
os.environ["http_proxy"] = ""
os.environ["https_proxy"] = ""

_client: OpenAI | None = None


def create_llm_client() -> OpenAI:
    """创建 OpenAI-compatible 客户端单例。"""
    global _client
    if _client is not None:
        return _client
    settings = load_settings()
    _client = OpenAI(
        api_key=settings["llm_api_key"],
        base_url=settings["llm_base_url"],
        timeout=120.0,
    )
    return _client


def _extract_json(text: str) -> dict:
    """从 LLM 响应中提取 JSON，兼容 markdown 代码块、多余文字等情况。"""
    text = text.strip()

    # 优先从 markdown 代码块中提取
    m = re.search(r"```(?:json)?\s*\n?(.*?)```", text, re.DOTALL)
    if m:
        text = m.group(1).strip()

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass

    # fallback: 提取第一个 { 到最后一个 } 之间的内容
    start = text.find("{")
    end = text.rfind("}")
    if start != -1 and end > start:
        try:
            return json.loads(text[start:end + 1])
        except json.JSONDecodeError:
            pass

    logger.error("无法从 LLM 响应中提取 JSON，原始文本:\n%s", text[:2000])
    return {}


_KEY_ALIASES: dict[str, str] = {
    "sql_template": "sqlTemplate",
    "sql": "sqlTemplate",
    "query": "sqlTemplate",
    "params_spec": "paramsSpec",
    "parameters": "paramsSpec",
    "params": "paramsSpec",
    "chart_hint": "chartHint",
    "chart": "chartHint",
}


def _normalize_keys(raw: dict) -> dict:
    """将 snake_case / 别名 key 统一映射到 camelCase，兼容小模型输出。"""
    normalized: dict = {}
    for k, v in raw.items():
        mapped = _KEY_ALIASES.get(k, k)
        if mapped not in normalized:
            normalized[mapped] = v
    return normalized


NUM_CTX = 8192  # Ollama 默认 num_ctx=2048，对 qwen2.5-coder:7b（原生 32K）严重欠配


def call_llm_json(messages: list[dict], temperature: float = 0.1) -> dict:
    """调用 LLM 并解析 JSON 响应。"""
    client = create_llm_client()
    settings = load_settings()

    total_chars = sum(len(m.get("content", "")) for m in messages)
    estimated_tokens = total_chars // 2
    logger.info(
        "Prompt 总字符数: %d, 估算 token 数: ~%d, num_ctx 上限: %d",
        total_chars, estimated_tokens, NUM_CTX,
    )

    response = client.chat.completions.create(
        model=settings["llm_model_id"],
        messages=messages,
        temperature=temperature,
        extra_body={"options": {"num_ctx": NUM_CTX}},
    )

    content = response.choices[0].message.content or ""
    logger.info("LLM 原始响应前 500 字符: %s", content[:500])

    if not content.strip():
        logger.warning("LLM 返回了空内容")
        return {}

    raw = _extract_json(content)
    normalized = _normalize_keys(raw)

    if not normalized.get("sqlTemplate"):
        logger.warning(
            "LLM 响应缺少 sqlTemplate，原始 key: %s，完整响应:\n%s",
            list(raw.keys()), content[:2000],
        )

    return normalized
