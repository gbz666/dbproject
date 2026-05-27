package com.database.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlTableExtractorTest {

    @Test
    void blankReturnsEmpty() {
        assertEquals("", SqlTableExtractor.extract(null));
        assertEquals("", SqlTableExtractor.extract(""));
    }

    @Test
    void singleTable() {
        String r = SqlTableExtractor.extract("SELECT * FROM customers WHERE is_deleted = 0");
        assertEquals("customers", r);
    }

    @Test
    void joinMultipleTables() {
        String sql = "SELECT p.product_name, SUM(soi.quantity) " +
                "FROM sales_order_items soi " +
                "JOIN products p ON soi.product_id = p.id " +
                "JOIN sales_orders so ON soi.sales_order_id = so.id";
        String r = SqlTableExtractor.extract(sql);
        // 按字典序排
        assertEquals("products,sales_order_items,sales_orders", r);
    }

    @Test
    void caseInsensitive() {
        String r = SqlTableExtractor.extract("select * from CUSTOMERS");
        assertEquals("customers", r);
    }

    @Test
    void backtickWrapped() {
        String r = SqlTableExtractor.extract("SELECT * FROM `products`");
        assertEquals("products", r);
    }

    @Test
    void schemaPrefixStripped() {
        String r = SqlTableExtractor.extract("SELECT * FROM dbproject.customers");
        assertEquals("customers", r);
    }

    @Test
    void unknownTableFiltered() {
        // foo_bar 不在白名单，应丢弃；customers 保留
        String r = SqlTableExtractor.extract("SELECT * FROM customers c JOIN foo_bar f ON c.id = f.cid");
        assertEquals("customers", r);
    }

    @Test
    void cteAliasNotMistaken() {
        // CTE x 不应被当作表
        String sql = "WITH x AS (SELECT id FROM customers) SELECT * FROM x";
        String r = SqlTableExtractor.extract(sql);
        // 只有 customers 被白名单接受
        assertEquals("customers", r);
    }

    @Test
    void duplicatesDeduped() {
        String sql = "SELECT * FROM products p1 JOIN products p2 ON p1.id = p2.id";
        String r = SqlTableExtractor.extract(sql);
        assertEquals("products", r);
    }
}
