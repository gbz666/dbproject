import logging
import re
from pathlib import Path

from aiagent.core.config import get_project_root

logger = logging.getLogger(__name__)

_CORE_TABLES = [
    "products", "product_categories",
    "sales_orders", "sales_order_items",
    "purchase_orders", "purchase_order_items",
    "customers", "suppliers",
    "inventory", "warehouses",
]

_SECONDARY_TABLES = [
    "outbound_orders", "outbound_order_items",
    "stock_ins", "stock_in_items",
    "sales_invoices", "sales_invoice_details",
    "purchase_invoices", "purchase_invoice_details",
    "payment_receipts", "payment_expenses",
    "staffs", "roles", "staff_roles", "contacts",
]

_AUDIT_COLUMNS = {
    "created_at", "updated_at", "deleted_at",
    "created_by_id", "updated_by_id",
}

MAX_SCHEMA_CHARS = 6000


def get_default_db_sql_path() -> Path:
    """返回项目根目录下 db.sql 的路径。"""
    return get_project_root() / "db.sql"


def _extract_all_tables(db_sql_text: str) -> dict[str, str]:
    """解析 db.sql，返回 {table_name: raw_body_and_after} 映射。"""
    table_pattern = re.compile(
        r"CREATE\s+TABLE\s+(\w+)\s*\((.*?)\)\s*ENGINE\s*=",
        re.DOTALL | re.IGNORECASE,
    )
    tables: dict[str, str] = {}
    for m in table_pattern.finditer(db_sql_text):
        name = m.group(1)
        body = m.group(2)
        after = db_sql_text[m.end():m.end() + 200]
        tables[name] = body + "\n---AFTER---\n" + after
    return tables


def _format_table(table_name: str, raw: str, filter_audit: bool = True) -> str:
    """将单个表的原始建表体格式化为精简摘要。"""
    parts = raw.split("\n---AFTER---\n", 1)
    body = parts[0]
    after = parts[1] if len(parts) > 1 else ""

    table_comment_pattern = re.compile(r"COMMENT\s*=\s*'([^']*)'", re.IGNORECASE)
    col_pattern = re.compile(
        r"^\s*(\w+)\s+(BIGINT|INT|TINYINT|VARCHAR\([^)]*\)|DECIMAL\([^)]*\)|TEXT|DATE|DATETIME|ENUM\([^)]*\)|JSON)"
        r"(?:.*?COMMENT\s+'([^']*)')?",
        re.IGNORECASE,
    )

    tc_match = table_comment_pattern.search(after[:200])
    table_comment = tc_match.group(1) if tc_match else ""

    header = f"TABLE {table_name}"
    if table_comment:
        header += f" ({table_comment})"

    lines = [header]
    for raw_line in body.split("\n"):
        stripped = raw_line.strip()
        if not stripped or stripped.startswith("--"):
            continue
        if any(stripped.upper().startswith(kw) for kw in
               ("CONSTRAINT", "PRIMARY KEY", "UNIQUE KEY", "INDEX", "FOREIGN KEY", "KEY ")):
            continue
        cm = col_pattern.match(stripped)
        if not cm:
            continue
        col_name = cm.group(1)
        col_type = cm.group(2)
        col_comment = cm.group(3) or ""

        if filter_audit and col_name.lower() in _AUDIT_COLUMNS:
            continue

        pk_marker = ""
        if "PRIMARY KEY" in stripped.upper() or "AUTO_INCREMENT" in stripped.upper():
            pk_marker = " PK"
        entry = f"  {col_name} {col_type}{pk_marker}"
        if col_comment:
            entry += f" -- {col_comment}"
        lines.append(entry)

    return "\n".join(lines)


def extract_table_summaries(db_sql_text: str) -> str:
    """
    从 db.sql 建表脚本中提取表名与列信息的精简摘要。
    核心业务表优先输出，过滤审计字段以减少 token 量，
    超过长度阈值时截断并附加警告。
    """
    all_tables = _extract_all_tables(db_sql_text)

    ordered_names: list[str] = []
    for name in _CORE_TABLES:
        if name in all_tables:
            ordered_names.append(name)
    for name in _SECONDARY_TABLES:
        if name in all_tables:
            ordered_names.append(name)
    for name in all_tables:
        if name not in ordered_names:
            ordered_names.append(name)

    blocks: list[str] = []
    total_len = 0
    truncated = False

    for name in ordered_names:
        block = _format_table(name, all_tables[name], filter_audit=True)
        block_len = len(block)
        if total_len + block_len > MAX_SCHEMA_CHARS:
            truncated = True
            logger.warning(
                "Schema 超过 %d 字符限制，已截断。包含 %d/%d 张表。",
                MAX_SCHEMA_CHARS, len(blocks), len(ordered_names),
            )
            break
        blocks.append(block)
        total_len += block_len

    result = "\n\n".join(blocks)
    if truncated:
        result += f"\n\n-- [注意] Schema 已截断，共 {len(all_tables)} 张表仅展示 {len(blocks)} 张核心表"
    return result


def load_db_schema_text(db_sql_path: Path | None = None) -> str:
    """读取 db.sql 并返回精简的 schema 摘要文本。"""
    if db_sql_path is None:
        db_sql_path = get_default_db_sql_path()
    if not db_sql_path.exists():
        return ""
    raw = db_sql_path.read_text(encoding="utf-8")
    return extract_table_summaries(raw)
