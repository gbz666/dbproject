"""
Schema Linking：根据用户的自然语言问题，从全量 schema 中挑选相关的 5-8 张表，
只把这部分 schema 注入 LLM prompt。

策略：
  1. 业务词典精确匹配（中文业务词 → 表名列表）
  2. 兜底：完全没命中时返回核心销售/产品表组合
  3. 上限 cap=8 张表，避免 prompt 爆炸

设计决策：
  - 不做 comment 词袋匹配/向量检索 —— 字典已 cover 90%+ 场景，复杂度不必要
  - 多词命中取并集而非交集 —— "产品的利润" → 产品表 ∪ 利润相关表
  - "利润/收入/成本" 这种抽象词必须显式映射到含 unit_price/cost_price 的具体表
"""
import logging
from typing import Iterable

from aiagent.core.schema_loader import TableInfo, render_tables

logger = logging.getLogger(__name__)


# ─── 业务词典 ────────────────────────────────────────────────────────────────
# 维护原则：
#   - key 是中文业务词或常用英文（如 top）
#   - value 是该业务概念可能涉及的所有表（保守冗余，宁可多召不漏）
#   - 隐式连接很关键：用户问 "利润" 不会说 "销售"，但 SQL 必然要 join sales_*
#   - 成本拆分：泛词 "成本" 默认指销售成本（算利润最常用），"采购成本/进货成本" 才走采购链

BUSINESS_TERMS: dict[str, list[str]] = {
    # === 销售域 ===
    "销售": ["sales_orders", "sales_order_items", "products", "customers"],
    "营业额": ["sales_orders", "sales_order_items"],
    "卖": ["sales_orders", "sales_order_items"],
    "订单": ["sales_orders", "sales_order_items"],  # 默认指销售订单（业务直觉）

    # === 采购域 ===
    "采购": ["purchase_orders", "purchase_order_items", "products", "suppliers"],
    "进货": ["purchase_orders", "purchase_order_items", "suppliers"],
    "采买": ["purchase_orders", "purchase_order_items", "suppliers"],

    # === 利润/收入/成本（关键：隐式连接） ===
    "利润": ["sales_orders", "sales_order_items", "products", "customers"],
    "盈利": ["sales_orders", "sales_order_items", "products"],
    "毛利": ["sales_orders", "sales_order_items", "products"],
    "收入": ["sales_orders", "sales_order_items", "payment_receipts"],
    "支出": ["purchase_orders", "purchase_order_items", "payment_expenses"],
    "成本": ["sales_order_items", "products"],  # 默认指销售成本（含 cost_price）
    "销售成本": ["sales_order_items", "products"],
    "采购成本": ["purchase_orders", "purchase_order_items", "suppliers"],
    "进货成本": ["purchase_orders", "purchase_order_items", "suppliers"],

    # === 库存/仓储 ===
    "库存": ["inventory", "warehouses", "products"],
    "在库": ["inventory", "warehouses"],
    "仓库": ["warehouses", "inventory"],

    # === 出入库 ===
    "入库": ["stock_ins", "stock_in_items", "products", "warehouses", "suppliers"],
    "出库": ["outbound_orders", "outbound_order_items", "products", "warehouses"],
    "发货": ["outbound_orders", "outbound_order_items", "customers"],
    "收货": ["stock_ins", "stock_in_items", "suppliers"],

    # === 财务对账 ===
    "对账": ["payment_receipts", "payment_expenses", "sales_invoices", "purchase_invoices"],
    "收款": ["payment_receipts", "sales_invoices", "customers"],
    "付款": ["payment_expenses", "purchase_invoices", "suppliers"],
    "应收": ["sales_invoices", "payment_receipts", "customers"],
    "应付": ["purchase_invoices", "payment_expenses", "suppliers"],
    "欠款": ["sales_invoices", "payment_receipts"],

    # === 发票 ===
    "发票": ["sales_invoices", "sales_invoice_details", "purchase_invoices", "purchase_invoice_details"],
    "销项": ["sales_invoices", "sales_invoice_details", "customers"],
    "进项": ["purchase_invoices", "purchase_invoice_details", "suppliers"],
    "开票": ["sales_invoices", "sales_invoice_details"],

    # === 主体 ===
    "客户": ["customers", "contacts"],
    "顾客": ["customers"],
    "供应商": ["suppliers", "contacts"],
    "联系人": ["contacts", "customers", "suppliers"],

    "产品": ["products", "product_categories"],
    "商品": ["products", "product_categories"],
    "物料": ["products"],
    "分类": ["product_categories"],
    "类别": ["product_categories"],

    "员工": ["staffs", "staff_roles", "roles"],
    "销售员": ["staffs"],
    "业务员": ["staffs"],
}


# ─── 参数 ────────────────────────────────────────────────────────────────────

# 完全没命中时的兜底表组合（最常被问的核心业务表）
_FALLBACK_TABLES = [
    "sales_orders", "sales_order_items", "products", "customers", "product_categories",
]

# 单次最多召回的表数（避免 prompt 爆炸；qwen 7B 的上下文也吃紧）
MAX_TABLES = 8


# ─── 选表逻辑 ────────────────────────────────────────────────────────────────

def select_tables_for_question(question: str, all_tables: dict[str, TableInfo]) -> list[str]:
    """
    根据 question 在 BUSINESS_TERMS 里扫一遍，返回命中的表名列表（去重+保序，最多 MAX_TABLES 张）。

    保序规则：按字典中"先匹配先入"的顺序，方便 LLM 看 prompt 时关注核心表。
    """
    if not question:
        return _trim(_FALLBACK_TABLES, all_tables)

    remaining = question.lower()
    matched: list[str] = []
    seen: set[str] = set()
    hit_terms: list[str] = []  # 仅 log 用

    # 业务词典按 key 长度降序扫；命中后从 remaining 里消除该子串，
    # 防止短词（如 "成本"）再次匹配同一段（如 "采购成本"）造成跨域多召
    sorted_terms = sorted(BUSINESS_TERMS.keys(), key=len, reverse=True)
    for term in sorted_terms:
        term_lower = term.lower()
        if term_lower in remaining:
            hit_terms.append(term)
            for table in BUSINESS_TERMS[term]:
                if table not in seen and table in all_tables:
                    seen.add(table)
                    matched.append(table)
            remaining = remaining.replace(term_lower, " ")

    if not matched:
        logger.info("Schema selector: 未命中业务词典，回退到核心表 [%s]", ",".join(_FALLBACK_TABLES))
        return _trim(_FALLBACK_TABLES, all_tables)

    if len(matched) > MAX_TABLES:
        logger.info("Schema selector: 命中 %d 张表（>%d），截断到 top %d",
                    len(matched), MAX_TABLES, MAX_TABLES)
        matched = matched[:MAX_TABLES]

    logger.info("Schema selector: 命中词 [%s] → 选表 [%s]",
                ",".join(hit_terms), ",".join(matched))
    return matched


def _trim(names: Iterable[str], all_tables: dict[str, TableInfo]) -> list[str]:
    """过滤掉数据库里不存在的表名（防 schema 演进时词典过期）"""
    return [n for n in names if n in all_tables][:MAX_TABLES]


# ─── 集成入口 ───────────────────────────────────────────────────────────────

def render_selected_schema(question: str, all_tables: dict[str, TableInfo]) -> str:
    """
    按问题选 5-8 张相关表，渲染成 LLM-friendly 文本。
    这是给 sql_agent.build_system_prompt 调用的主入口。
    """
    selected = select_tables_for_question(question, all_tables)
    return render_tables(all_tables, table_names=selected, filter_audit=True)
