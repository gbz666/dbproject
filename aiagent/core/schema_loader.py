"""
db.sql 解析器：把 CREATE TABLE 语句切成结构化的 TableInfo/ColumnInfo，
供 schema_selector 按用户问题挑选相关表注入 LLM prompt。

向后兼容：load_db_schema_text() 仍然返回拼好的全量文本，老调用方不动。
"""
import logging
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Iterable

from aiagent.core.config import get_project_root

logger = logging.getLogger(__name__)


# ─── 数据结构 ────────────────────────────────────────────────────────────────

@dataclass
class ColumnInfo:
    name: str
    type: str
    comment: str = ""
    is_pk: bool = False
    is_audit: bool = False  # created_at / updated_at / deleted_at / created_by_id ...


@dataclass
class TableInfo:
    name: str
    comment: str = ""
    columns: list[ColumnInfo] = field(default_factory=list)

    def render(self, filter_audit: bool = True) -> str:
        """格式化为 LLM-friendly 文本块。"""
        header = f"TABLE {self.name}"
        if self.comment:
            header += f" ({self.comment})"
        lines = [header]
        for col in self.columns:
            if filter_audit and col.is_audit:
                continue
            pk_marker = " PK" if col.is_pk else ""
            entry = f"  {col.name} {col.type}{pk_marker}"
            if col.comment:
                entry += f" -- {col.comment}"
            lines.append(entry)
        return "\n".join(lines)


# ─── 解析常量 ────────────────────────────────────────────────────────────────

_AUDIT_COLUMN_NAMES = {
    "created_at", "updated_at", "deleted_at",
    "created_by_id", "updated_by_id", "is_deleted",
}

_CORE_TABLES_ORDER = [
    "products", "product_categories",
    "sales_orders", "sales_order_items",
    "purchase_orders", "purchase_order_items",
    "customers", "suppliers",
    "inventory", "warehouses",
]

_SECONDARY_TABLES_ORDER = [
    "outbound_orders", "outbound_order_items",
    "stock_ins", "stock_in_items",
    "sales_invoices", "sales_invoice_details",
    "purchase_invoices", "purchase_invoice_details",
    "payment_receipts", "payment_expenses",
    "staffs", "roles", "staff_roles", "contacts",
]

MAX_SCHEMA_CHARS = 20000

# 用于切单列定义；注意：行内若含 "DECIMAL(18,2)" 这种内部逗号，按行扫不按逗号切。
_COL_LINE_PATTERN = re.compile(
    r"^\s*(\w+)\s+(BIGINT|INT|TINYINT|VARCHAR\([^)]*\)|DECIMAL\([^)]*\)|TEXT|DATE|DATETIME|ENUM\([^)]*\)|JSON)"
    r"(?:.*?COMMENT\s+'([^']*)')?",
    re.IGNORECASE | re.DOTALL,
)

_TABLE_BLOCK_PATTERN = re.compile(
    r"CREATE\s+TABLE\s+(\w+)\s*\((.*?)\)\s*ENGINE\s*=[^;]*;",
    re.DOTALL | re.IGNORECASE,
)

_TABLE_COMMENT_INLINE_PATTERN = re.compile(
    r"ENGINE\s*=\s*\w+\s*(?:DEFAULT\s+CHARSET\s*=\s*\w+\s*)?COMMENT\s*=\s*'([^']*)'",
    re.IGNORECASE,
)


# ─── 解析 ────────────────────────────────────────────────────────────────────

def get_default_db_sql_path() -> Path:
    """返回项目根目录下 db.sql 的路径。"""
    return get_project_root() / "db.sql"


def parse_db_sql(db_sql_text: str) -> dict[str, TableInfo]:
    """把 db.sql 全文解析成 {table_name: TableInfo} 字典。"""
    tables: dict[str, TableInfo] = {}
    for m in _TABLE_BLOCK_PATTERN.finditer(db_sql_text):
        name = m.group(1)
        body = m.group(2)
        # 表注释在 ENGINE=... COMMENT='...' 处
        tail = db_sql_text[m.end() - 300:m.end()]
        tc_match = _TABLE_COMMENT_INLINE_PATTERN.search(tail)
        table_comment = tc_match.group(1) if tc_match else ""

        columns = list(_parse_columns(body))
        tables[name] = TableInfo(name=name, comment=table_comment, columns=columns)
    return tables


def _parse_columns(body: str) -> Iterable[ColumnInfo]:
    """从 CREATE TABLE 体内逐行抽列定义；按物理行扫，避免被 DECIMAL(18,2) 的逗号干扰。"""
    for raw_line in body.split("\n"):
        line = raw_line.strip().rstrip(",")
        if not line or line.startswith("--"):
            continue
        upper = line.upper()
        if any(upper.startswith(kw) for kw in
               ("CONSTRAINT", "PRIMARY KEY", "UNIQUE KEY", "INDEX", "FOREIGN KEY", "KEY ")):
            continue
        m = _COL_LINE_PATTERN.match(line)
        if not m:
            continue
        col_name = m.group(1)
        col_type = m.group(2)
        col_comment = m.group(3) or ""
        is_pk = "PRIMARY KEY" in upper or "AUTO_INCREMENT" in upper
        is_audit = col_name.lower() in _AUDIT_COLUMN_NAMES
        yield ColumnInfo(
            name=col_name,
            type=col_type,
            comment=col_comment,
            is_pk=is_pk,
            is_audit=is_audit,
        )


# ─── 渲染（向后兼容 + schema_selector 用） ──────────────────────────────────────

def render_tables(tables: dict[str, TableInfo], table_names: Iterable[str] | None = None,
                  filter_audit: bool = True) -> str:
    """按给定顺序渲染指定的表为单一文本块；table_names=None 表示全量按 core/secondary 顺序。"""
    if table_names is None:
        ordered = list(_default_table_order(tables.keys()))
    else:
        ordered = [n for n in table_names if n in tables]

    blocks: list[str] = []
    total = 0
    truncated = False
    for name in ordered:
        block = tables[name].render(filter_audit=filter_audit)
        if total + len(block) > MAX_SCHEMA_CHARS:
            truncated = True
            logger.warning("Schema 超过 %d 字符上限，已截断（输出 %d/%d 张表）",
                           MAX_SCHEMA_CHARS, len(blocks), len(ordered))
            break
        blocks.append(block)
        total += len(block)

    out = "\n\n".join(blocks)
    if truncated:
        out += f"\n\n-- [注意] 共 {len(tables)} 张表仅展示 {len(blocks)} 张"
    return out


def _default_table_order(all_names: Iterable[str]) -> list[str]:
    name_set = set(all_names)
    ordered = [n for n in _CORE_TABLES_ORDER if n in name_set]
    ordered += [n for n in _SECONDARY_TABLES_ORDER if n in name_set]
    ordered += [n for n in name_set if n not in ordered]
    return ordered


# ─── 公开入口 ───────────────────────────────────────────────────────────────

def load_db_schema_structured(db_sql_path: Path | None = None) -> dict[str, TableInfo]:
    """加载 db.sql 并返回结构化字典。供 schema_selector 调用。"""
    if db_sql_path is None:
        db_sql_path = get_default_db_sql_path()
    if not db_sql_path.exists():
        logger.warning("db.sql 不存在: %s", db_sql_path)
        return {}
    raw = db_sql_path.read_text(encoding="utf-8")
    return parse_db_sql(raw)


def load_db_schema_text(db_sql_path: Path | None = None) -> str:
    """读取 db.sql 返回全量精简文本（向后兼容老调用方）。"""
    tables = load_db_schema_structured(db_sql_path)
    if not tables:
        return ""
    return render_tables(tables, table_names=None, filter_audit=True)
