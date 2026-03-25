import json

from openai import OpenAI

from aiagent.core.config import load_settings


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
    )
    return _client


def call_llm_json(messages: list[dict], temperature: float = 0) -> dict:
    """
    调用 LLM 并解析 JSON 响应。
    messages 格式：[{"role": "system"|"user", "content": "..."}]
    """
    client = create_llm_client()
    settings = load_settings()
    response = client.chat.completions.create(
        model=settings["llm_model_id"],
        messages=messages,
        temperature=temperature,
        response_format={"type": "json_object"},
    )
    content = response.choices[0].message.content or "{}"
    return json.loads(content)
