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

    输出格式示例:
      TABLE staffs (员工表:存放平台员工信息)
        id BIGINT PK -- 员工主键ID
        staff_name VARCHAR(100) -- 员工姓名
        ...
    """
    table_pattern = re.compile(
        r"CREATE\s+TABLE\s+(\w+)\s*\((.*?)\)\s*ENGINE\s*=",
        re.DOTALL | re.IGNORECASE,
    )
    table_comment_pattern = re.compile(
        r"COMMENT\s*=\s*'([^']*)'",
        re.IGNORECASE,
    )
    col_pattern = re.compile(
        r"^\s*(\w+)\s+(BIGINT|INT|TINYINT|VARCHAR\([^)]*\)|DECIMAL\([^)]*\)|TEXT|DATE|DATETIME|ENUM\([^)]*\)|JSON)"
        r"(?:.*?COMMENT\s+'([^']*)')?"
        ,
        re.IGNORECASE,
    )

    lines: list[str] = []

    for match in table_pattern.finditer(db_sql_text):
        table_name = match.group(1)
        body = match.group(2)

        after = db_sql_text[match.end():]
        tc_match = table_comment_pattern.search(after[:200])
        table_comment = tc_match.group(1) if tc_match else ""

        header = f"TABLE {table_name}"
        if table_comment:
            header += f" ({table_comment})"
        lines.append(header)

        for raw_line in body.split("\n"):
            stripped = raw_line.strip()
            if not stripped or stripped.startswith("--"):
                continue
            if any(stripped.upper().startswith(kw) for kw in
                   ("CONSTRAINT", "PRIMARY KEY", "UNIQUE KEY", "INDEX", "FOREIGN KEY", "KEY ")):
                continue
            cm = col_pattern.match(stripped)
            if cm:
                col_name = cm.group(1)
                col_type = cm.group(2)
                col_comment = cm.group(3) or ""
                pk_marker = ""
                if "PRIMARY KEY" in stripped.upper() or "AUTO_INCREMENT" in stripped.upper():
                    pk_marker = " PK"
                entry = f"  {col_name} {col_type}{pk_marker}"
                if col_comment:
                    entry += f" -- {col_comment}"
                lines.append(entry)

        lines.append("")

    return "\n".join(lines).strip()


def load_db_schema_text(db_sql_path: Path | None = None) -> str:
    """读取 db.sql 并返回精简的 schema 摘要文本。"""
    if db_sql_path is None:
        db_sql_path = get_default_db_sql_path()
    if not db_sql_path.exists():
        return ""
    raw = db_sql_path.read_text(encoding="utf-8")
    return extract_table_summaries(raw)
