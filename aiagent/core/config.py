import os
from pathlib import Path

from dotenv import load_dotenv


def get_project_root() -> Path:
    """返回项目根目录（aiagent 的上级目录）。"""
    return Path(__file__).resolve().parent.parent.parent


def load_env() -> None:
    """加载 aiagent/.env 或项目根目录的 .env。"""
    aiagent_env = Path(__file__).resolve().parent.parent / ".env"
    if aiagent_env.exists():
        load_dotenv(dotenv_path=aiagent_env)
    else:
        load_dotenv()


def load_settings() -> dict:
    """从环境变量读取服务配置。"""
    load_env()
    return {
        "llm_base_url": os.getenv("LLM_BASE_URL", ""),
        "llm_model_id": os.getenv("LLM_MODEL_ID", ""),
        "llm_api_key": os.getenv("LLM_API_KEY", ""),
        "chart_output_dir": os.getenv("CHART_OUTPUT_DIR", str(get_project_root() / "charts")),
        "host": os.getenv("AIAGENT_HOST", "0.0.0.0"),
        "port": int(os.getenv("AIAGENT_PORT", "8001")),
    }


def get_chart_output_dir() -> Path:
    """返回图表输出目录（相对路径基于项目根目录解析，避免受 CWD 影响）。"""
    settings = load_settings()
    raw = Path(settings["chart_output_dir"])
    if raw.is_absolute():
        resolved = raw
    else:
        resolved = (get_project_root() / raw).resolve()
    import logging
    logging.getLogger(__name__).info("图表输出目录: %s (project_root=%s)", resolved, get_project_root())
    return resolved
