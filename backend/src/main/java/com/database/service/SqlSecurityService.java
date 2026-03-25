package com.database.service;

import com.database.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SQL 安全校验服务：拦截非法 SQL，防止注入与越权操作
 */
@Service
@Slf4j
public class SqlSecurityService {

    @Value("${aiagent.sql.max-rows:100}")
    private int maxRows;

    private static final List<String> FORBIDDEN_KEYWORDS = List.of(
            "insert", "update", "delete", "drop", "alter", "truncate",
            "grant", "revoke", "create", "replace", "rename", "call",
            "load", "outfile", "dumpfile"
    );

    private static final List<String> FORBIDDEN_SCHEMAS = List.of(
            "information_schema", "mysql", "performance_schema", "sys"
    );

    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";");

    /**
     * 对最终 SQL 做安全校验，不通过抛出 BusinessException
     */
    public void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new BusinessException("SQL 不能为空", 400);
        }

        String trimmed = sql.strip();
        String upper = trimmed.toUpperCase();

        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            throw new BusinessException("仅允许 SELECT / WITH 查询", 400);
        }

        if (SEMICOLON_PATTERN.matcher(trimmed).find()) {
            throw new BusinessException("SQL 中禁止包含分号（防止多语句注入）", 400);
        }

        String lower = trimmed.toLowerCase();
        for (String kw : FORBIDDEN_KEYWORDS) {
            if (Pattern.compile("\\b" + kw + "\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
                throw new BusinessException("SQL 包含禁止的关键词: " + kw, 400);
            }
        }

        for (String schema : FORBIDDEN_SCHEMAS) {
            if (lower.contains(schema)) {
                throw new BusinessException("SQL 中禁止访问系统库: " + schema, 400);
            }
        }
    }

    /**
     * 将 SQL 模板中的 {param} 占位符替换为实际参数值（字符串会加引号）
     */
    public String renderSql(String sqlTemplate, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return sqlTemplate;
        }
        String result = sqlTemplate;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            Object val = entry.getValue();
            String replacement;
            if (val instanceof Number) {
                replacement = val.toString();
            } else {
                String escaped = val.toString().replace("'", "''");
                replacement = "'" + escaped + "'";
            }
            result = result.replace(placeholder, replacement);
        }
        return result;
    }

    /**
     * 确保 SQL 末尾有 LIMIT 限制，防止全表扫描
     */
    public String ensureLimit(String sql) {
        String upper = sql.strip().toUpperCase();
        if (!upper.contains("LIMIT")) {
            return sql.strip() + " LIMIT " + maxRows;
        }
        return sql;
    }
}
