package com.database.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * EasyExcel 行数据读取工具：从「列名 → 值」的 Map 中按多别名取数，支持部分列缺失。
 */
public final class ExcelRowReadHelper {

    private ExcelRowReadHelper() {}

    /**
     * 从行 Map 中按多个可能的列名取第一个非空字符串（列名支持包含关系匹配）。
     */
    public static String get(Map<String, String> row, String... keys) {
        if (row == null) return "";
        for (String key : keys) {
            if (key == null) continue;
            for (Map.Entry<String, String> e : row.entrySet()) {
                if (e.getKey() != null && (e.getKey().equals(key) || e.getKey().contains(key) || key.contains(e.getKey()))) {
                    String v = e.getValue();
                    if (v != null && !v.trim().isEmpty()) return v.trim();
                }
            }
        }
        return "";
    }

    public static BigDecimal safeDecimal(String s, BigDecimal def) {
        if (s == null || s.trim().isEmpty()) return def;
        try {
            return new BigDecimal(s.replace("¥", "").replace(",", "").trim());
        } catch (Exception e) {
            return def;
        }
    }

    public static int safeInt(String s, int def) {
        if (s == null || s.trim().isEmpty()) return def;
        try {
            return Integer.parseInt(s.replace(",", "").trim());
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 解析日期字符串。支持：
     * - "yyyy-MM-dd" 或含 '-' 的日期格式；
     * - Excel 日期序列数（数字，表示自 1900-01-01 起的天数）。
     */
    public static Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            if (s.contains("-")) {
                return Date.from(LocalDate.parse(s.substring(0, Math.min(10, s.length())).trim()).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            // Excel 日期序列：1 = 1900-01-01
            double serial = Double.parseDouble(s.trim());
            long days = (long) serial;
            LocalDate d = LocalDate.of(1900, 1, 1).plusDays(days - 1);
            return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    /** 规范化表头：去换行、空格，便于与 DTO 中定义的别名一致 */
    public static String normalizeHeader(String s) {
        return s == null ? "" : s.replace("\n", "").replace("\r", "").trim();
    }
}
