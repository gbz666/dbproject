import re
from pathlib import Path

from aiagent.core.config import get_project_root


def get_default_db_sql_path() -> Path:
    """返回项目根目录下 db.sql 的路径。"""
    return get_project_root() / "db.sql"


def extract_table_summaries(db_sql_text: str) -> str:
    """
    从 db.sql 建表脚本中提取表名与列信息的精简摘要。
    用于拼入 LLM 系统提示词，控制 token 量。
    """
    pass


def load_db_schema_text(db_sql_path: Path) -> str:
    """读取 db.sql 并返回精简的 schema 摘要文本。"""
    if not db_sql_path.exists():
        return ""
    raw = db_sql_path.read_text(encoding="utf-8")
    return extract_table_summaries(raw)
