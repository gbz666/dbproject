package com.database.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiSqlNormalizer 纯函数单测，不依赖 Spring。
 * 核心验证：同义/数字/时间被折叠成相同归一化串，确保 RAG 能匹配上。
 */
class AiSqlNormalizerTest {

    @Test
    void blankReturnsEmpty() {
        assertEquals("", AiSqlNormalizer.normalize(null));
        assertEquals("", AiSqlNormalizer.normalize(""));
        assertEquals("", AiSqlNormalizer.normalize("   "));
    }

    @Test
    void numberToPlaceholder() {
        String n1 = AiSqlNormalizer.normalize("销售前 10 的产品");
        String n2 = AiSqlNormalizer.normalize("销售前 20 的产品");
        // 数字归一化后两个问题应一样
        assertEquals(n1, n2);
        assertTrue(n1.contains("__num__"), "应含 __num__ 占位符: " + n1);
    }

    @Test
    void chineseNumberAlsoPlaceholder() {
        String n1 = AiSqlNormalizer.normalize("销售前十的产品");
        String n2 = AiSqlNormalizer.normalize("销售前10的产品");
        assertEquals(n1, n2);
    }

    @Test
    void timeToPlaceholder() {
        String n1 = AiSqlNormalizer.normalize("本季度销售");
        String n2 = AiSqlNormalizer.normalize("本月销售");
        // 时间词都折叠成 __time__，二者应该一样
        assertEquals(n1, n2);
        assertTrue(n1.contains("__time__"));
    }

    @Test
    void synonymFold() {
        String n1 = AiSqlNormalizer.normalize("产品销售额");
        String n2 = AiSqlNormalizer.normalize("商品营业额");
        assertEquals(n1, n2, "产品↔商品 / 销售额↔营业额 应折叠");
    }

    @Test
    void stopwordsRemoved() {
        String r = AiSqlNormalizer.normalize("查询本季度的销售前 10 的产品");
        assertFalse(r.contains("查询"), "查询应被去除: " + r);
        assertFalse(r.contains("的"), "的应被去除: " + r);
    }

    @Test
    void punctuationRemoved() {
        String r = AiSqlNormalizer.normalize("产品，销售。前 10！");
        assertFalse(r.contains("，"));
        assertFalse(r.contains("。"));
        assertFalse(r.contains("！"));
    }

    @Test
    void differentPhrasingsConverge() {
        // 课设答辩核心场景：用户用不同表述，归一化结果应一致
        String n1 = AiSqlNormalizer.normalize("查询本季度销售额前 10 的产品");
        String n2 = AiSqlNormalizer.normalize("看一下本季度营业额 top10 商品");
        // 这两个不可能完全等，但应该有共同字符
        // 重点验证：__time__ 销售额 __num__ 产品 都在
        assertTrue(n1.contains("__time__"), "n1 缺 __time__: " + n1);
        assertTrue(n1.contains("销售额"), "n1 缺 销售额: " + n1);
        assertTrue(n1.contains("__num__"), "n1 缺 __num__: " + n1);
        assertTrue(n1.contains("产品"), "n1 缺 产品: " + n1);
        assertTrue(n2.contains("销售额"), "n2 同义未折叠: " + n2);
        assertTrue(n2.contains("产品"), "n2 同义未折叠: " + n2);
    }

    @Test
    void caseInsensitive() {
        String n1 = AiSqlNormalizer.normalize("TOP 10");
        String n2 = AiSqlNormalizer.normalize("top 10");
        assertEquals(n1, n2);
    }
}
