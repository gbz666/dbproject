package com.database.service;

import com.database.mapper.AiSqlExecLogMapper;
import com.database.pojo.AiSqlExecLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI SQL 审计日志集成测试
 * 需要数据库环境运行：mysql -u root -p dbproject < scripts/ai_tables.sql
 */
@SpringBootTest
class AiSqlExecLogTest {

    @Autowired
    private AiSqlExecLogMapper aiSqlExecLogMapper;

    @Test
    void testInsertAndSelect() {
        // 构造一条测试日志
        AiSqlExecLog log = new AiSqlExecLog();
        log.setUserId(1L);
        log.setSqlHash(sha256("SELECT 1"));
        log.setSqlText("SELECT 1");
        log.setParams("{\"test\": true}");
        log.setRowCount(1);
        log.setLatencyMs(42);
        log.setStatus("success");

        // 插入
        aiSqlExecLogMapper.insert(log);
        assertNotNull(log.getId(), "插入后应返回自增 ID");

        // 按用户查询
        List<AiSqlExecLog> logs = aiSqlExecLogMapper.selectByUserId(1L);
        assertFalse(logs.isEmpty(), "应能查询到刚插入的日志");

        AiSqlExecLog found = logs.get(0);
        assertEquals("success", found.getStatus());
        assertEquals("SELECT 1", found.getSqlText());
        assertEquals(Integer.valueOf(1), found.getRowCount());
        assertEquals(Integer.valueOf(42), found.getLatencyMs());
        assertNotNull(found.getCreatedAt());
    }

    @Test
    void testSelectRecent() {
        List<AiSqlExecLog> logs = aiSqlExecLogMapper.selectRecent(5);
        assertNotNull(logs);
        assertTrue(logs.size() <= 5, "返回条数不应超过 limit");
    }

    @Test
    void testSelectBySqlHash() {
        String hash = sha256("SELECT 1");
        List<AiSqlExecLog> logs = aiSqlExecLogMapper.selectBySqlHash(hash);
        assertNotNull(logs);
    }

    @Test
    void testInsertWithError() {
        AiSqlExecLog log = new AiSqlExecLog();
        log.setUserId(1L);
        log.setSqlHash(sha256("INVALID SQL"));
        log.setSqlText("INVALID SQL");
        log.setRowCount(0);
        log.setLatencyMs(10);
        log.setStatus("error");
        log.setErrorMsg("语法错误");

        aiSqlExecLogMapper.insert(log);
        assertNotNull(log.getId());

        List<AiSqlExecLog> logs = aiSqlExecLogMapper.selectBySqlHash(sha256("INVALID SQL"));
        assertFalse(logs.isEmpty());
        assertEquals("error", logs.get(0).getStatus());
        assertEquals("语法错误", logs.get(0).getErrorMsg());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
