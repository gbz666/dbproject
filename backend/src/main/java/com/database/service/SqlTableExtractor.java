package com.database.service;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从参数化 SQL 模板中提取涉及的表名。
 *
 * 用于写入 ai_sql_memory.tables_used，RAG 检索时按"表名重叠度"加权。
 * 用 schema 白名单二次过滤，丢弃 CTE 别名 / 子查询别名 / 拼写错误的表。
 */
public final class SqlTableExtractor {

    private SqlTableExtractor() {}

    /** db.sql 中现有的全部业务表（与 git 仓库 schema 保持一致；schema 变更时手动同步）*/
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "ai_conversations", "ai_messages", "ai_sql_exec_log",
            "ai_sql_feedback", "ai_sql_memory",
            "contacts", "customers",
            "inventory",
            "outbound_order_items", "outbound_orders",
            "payment_expenses", "payment_receipts",
            "product_categories", "products",
            "purchase_invoice_details", "purchase_invoices",
            "purchase_order_items", "purchase_orders",
            "roles",
            "sales_invoice_details", "sales_invoices",
            "sales_order_items", "sales_orders",
            "staff_roles", "staffs",
            "stock_in_items", "stock_ins",
            "suppliers", "warehouses"
    );

    /** 抓 FROM/JOIN 后的表名（容忍反引号、容忍 schema.table 前缀；表名首字符必须字母或下划线）*/
    private static final Pattern FROM_JOIN = Pattern.compile(
            "(?i)\\b(?:FROM|JOIN)\\s+`?(?:[a-z_][a-z0-9_]*\\.)?([a-z_][a-z0-9_]*)`?"
    );

    /**
     * @return 涉及表名按字典序拼成 "t1,t2,t3"，无命中返回空串
     */
    public static String extract(String sql) {
        if (sql == null || sql.isBlank()) return "";

        TreeSet<String> tables = new TreeSet<>();
        Matcher m = FROM_JOIN.matcher(sql);
        while (m.find()) {
            String name = m.group(1).toLowerCase();
            if (ALLOWED_TABLES.contains(name)) {
                tables.add(name);
            }
        }
        return String.join(",", tables);
    }
}
