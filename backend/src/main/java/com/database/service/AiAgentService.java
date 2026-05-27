package com.database.service;

import com.database.dto.AiExecuteSqlRequest;
import com.database.dto.AiExecuteSqlResponse;
import com.database.dto.AiGenerateSqlRequest;
import com.database.dto.AiGenerateSqlResponse;
import com.database.exception.BusinessException;
import com.database.mapper.AiSqlExecLogMapper;
import com.database.pojo.AiSqlExecLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * AI Agent 服务：与 Python AI 服务通信，处理 SQL 生成与执行
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiAgentService {

    private final RestTemplate restTemplate;
    private final SqlSecurityService sqlSecurityService;
    private final JdbcTemplate jdbcTemplate;
    private final AiSqlExecLogMapper aiSqlExecLogMapper;
    private final ObjectMapper objectMapper;
    private final AiMemoryService aiMemoryService;

    @Value("${aiagent.base-url:http://localhost:8001}")
    private String aiagentBaseUrl;

    @Value("${aiagent.sql.max-rows:100}")
    private int maxRows;

    @Value("${aiagent.sql.timeout-ms:15000}")
    private int timeoutMs;

    /**
     * 调用 Python 服务生成参数化 SQL 模板，同时把模板保存进 ai_sql_memory。
     *
     * @param userId 当前用户（用于 created_by）；可为 null（未登录调用 generate-sql 时）
     */
    public AiGenerateSqlResponse generateSql(AiGenerateSqlRequest request, Long userId) {
        String url = aiagentBaseUrl + "/generate-sql";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("question", request.getQuestion());
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            body.put("history", request.getHistory());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        AiGenerateSqlResponse response;
        try {
            ResponseEntity<AiGenerateSqlResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AiGenerateSqlResponse.class
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                response = resp.getBody();
            } else {
                throw new BusinessException("AI 服务返回异常", 502);
            }
        } catch (RestClientException e) {
            log.error("调用 Python AI 服务失败: {}", e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试", 503);
        }

        // 保存/复用记忆，把 memoryId 透传给前端
        try {
            Long memoryId = aiMemoryService.saveOrReuseMemory(request.getQuestion(), response, userId);
            response.setMemoryId(memoryId);
        } catch (Exception e) {
            log.warn("saveOrReuseMemory 失败（非致命）: {}", e.getMessage());
        }

        return response;
    }

    /**
     * 安全执行 SQL：参数替换 -> 安全校验 -> EXPLAIN 校验 -> 执行 -> 调图表服务
     * 执行结果（成功/失败）自动写入 ai_sql_exec_log 审计日志。
     */
    public AiExecuteSqlResponse executeSql(AiExecuteSqlRequest request, String question, Long userId) {
        String sqlTemplate = request.getSqlTemplate();
        Map<String, Object> params = request.getParams();
        Long memoryId = request.getMemoryId();

        String finalSql = sqlSecurityService.renderSql(sqlTemplate, params);
        finalSql = sqlSecurityService.ensureLimit(finalSql);
        String sqlHash = sha256(finalSql);

        // 审计日志对象，在 finally 中统一写入
        AiSqlExecLog auditLog = new AiSqlExecLog();
        auditLog.setMemoryId(memoryId);
        auditLog.setUserId(userId);
        auditLog.setSqlHash(sqlHash);
        auditLog.setSqlText(finalSql);
        auditLog.setParams(serializeParams(params));

        long start = System.currentTimeMillis();
        // 追踪当前阶段，用于异常时分类审计状态
        String execStage = "security_reject";
        try {
            sqlSecurityService.validateSql(finalSql);

            execStage = "explain_fail";
            runExplainCheck(finalSql);

            execStage = "error";

            jdbcTemplate.setQueryTimeout(timeoutMs / 1000);
            List<Map<String, Object>> resultMaps = jdbcTemplate.queryForList(finalSql);

            long elapsed = System.currentTimeMillis() - start;
            log.info("AI SQL 执行完成，耗时 {}ms，返回 {} 行", elapsed, resultMaps.size());

            List<String> columns = new ArrayList<>();
            List<List<Object>> rows = new ArrayList<>();

            if (!resultMaps.isEmpty()) {
                columns.addAll(resultMaps.get(0).keySet());
                for (Map<String, Object> row : resultMaps) {
                    List<Object> rowData = new ArrayList<>();
                    for (String col : columns) {
                        rowData.add(row.get(col));
                    }
                    rows.add(rowData);
                }
            }

            String chartUrl = null;
            if (request.getChartHint() != null && !rows.isEmpty()) {
                chartUrl = callGenerateChart(question, columns, rows, request.getChartHint());
            }

            // 审计日志：成功
            auditLog.setStatus("success");
            auditLog.setRowCount(rows.size());
            auditLog.setLatencyMs((int) (System.currentTimeMillis() - start));

            // 记忆统计：success_count++ 并重算置信度
            if (memoryId != null) {
                try {
                    aiMemoryService.recordExecSuccess(memoryId);
                } catch (Exception e) {
                    log.warn("recordExecSuccess 失败（非致命）: {}", e.getMessage());
                }
            }

            AiExecuteSqlResponse response = new AiExecuteSqlResponse();
            response.setColumns(columns);
            response.setRows(rows);
            response.setChartUrl(chartUrl);
            response.setSqlTemplate(sqlTemplate);
            response.setParams(params);
            return response;

        } catch (BusinessException e) {
            auditLog.setStatus(execStage);
            auditLog.setErrorMsg(e.getMessage());
            auditLog.setLatencyMs((int) (System.currentTimeMillis() - start));
            throw e;

        } catch (Exception e) {
            auditLog.setStatus("error");
            auditLog.setErrorMsg(e.getMessage());
            auditLog.setLatencyMs((int) (System.currentTimeMillis() - start));
            throw new BusinessException("SQL 执行失败: " + e.getMessage(), 400);

        } finally {
            // 审计日志写入失败不影响主流程
            try {
                aiSqlExecLogMapper.insert(auditLog);
            } catch (Exception e) {
                log.warn("审计日志写入失败: {}", e.getMessage());
            }
            // 失败时累计降级 memory，避免被 RAG 反复捞回（必须在 exec_log 插入之后）
            if (memoryId != null && !"success".equals(auditLog.getStatus())) {
                try {
                    aiMemoryService.recordExecFailure(memoryId);
                } catch (Exception e) {
                    log.warn("recordExecFailure 失败（非致命）: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 将 params Map 序列化为 JSON 字符串
     */
    private String serializeParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return params.toString();
        }
    }

    /**
     * 计算 SHA-256 哈希
     */
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 在所有 JVM 中都可用，不会走到这里
            return "";
        }
    }

    /**
     * 执行 EXPLAIN 校验，不通过则抛异常
     */
    private void runExplainCheck(String sql) {
        try {
            jdbcTemplate.queryForList("EXPLAIN " + sql);
        } catch (Exception e) {
            log.warn("EXPLAIN 校验失败: {}", e.getMessage());
            throw new BusinessException("SQL 语法校验未通过（EXPLAIN 失败）: " + e.getMessage(), 400);
        }
    }

    /**
     * 调用 Python /generate-chart 生成图表，返回可访问的 chartUrl
     */
    private String callGenerateChart(String question, List<String> columns,
                                     List<List<Object>> rows, Map<String, Object> chartHint) {
        String url = aiagentBaseUrl + "/generate-chart";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("question", question != null ? question : "查询结果");
        body.put("columns", columns);
        body.put("rows", rows);
        body.put("chartHint", chartHint);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                String chartId = (String) resp.getBody().get("chartId");
                return "/api/ai/chart/" + chartId;
            }
        } catch (RestClientException e) {
            log.warn("图表生成失败（非致命）: {}", e.getMessage());
        }
        return null;
    }

    @Value("${aiagent.db.readonly-url}")
    private String readonlyUrl;

    @Value("${aiagent.db.readonly-username}")
    private String readonlyUsername;

    @Value("${aiagent.db.readonly-password}")
    private String readonlyPassword;

    public com.database.dto.AiTrialExecuteResponse trialExecute(String sql) {
        com.database.dto.AiTrialExecuteResponse response = new com.database.dto.AiTrialExecuteResponse();
        
        try {
            // 前置安全拦截，确保哪怕是只读账号，也不要跑诸如包含;的多条语句
            sqlSecurityService.validateSql(sql);
            String finalSql = sqlSecurityService.ensureLimit(sql);
            
            // 手动创建 JDBC 连接使用只读账号
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(readonlyUrl, readonlyUsername, readonlyPassword)) {
                // 设置超时防止大查询
                try (java.sql.Statement stmt = conn.createStatement()) {
                    stmt.setQueryTimeout(10); // 10秒超时
                    try (java.sql.ResultSet rs = stmt.executeQuery(finalSql)) {
                        java.sql.ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        
                        List<String> columns = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            columns.add(metaData.getColumnLabel(i));
                        }
                        
                        List<List<Object>> rows = new ArrayList<>();
                        int count = 0;
                        while(rs.next() && count < 20) { // 最多取 20 行用于给 AI 参考
                            List<Object> row = new ArrayList<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.add(rs.getObject(i));
                            }
                            rows.add(row);
                            count++;
                        }
                        
                        response.setSuccess(true);
                        response.setColumns(columns);
                        response.setRows(rows);
                        response.setRowCount(count);
                        response.setError("");
                        log.info("MCP 试跑成功: {} 列, {} 行 (前 {} 行)", columnCount, count, rows.size());
                    }
                }
            } catch (java.sql.SQLException e) {
                // 返回原汁原味的数据库报错，供 LLM 修正
                response.setSuccess(false);
                response.setColumns(new ArrayList<>());
                response.setRows(new ArrayList<>());
                response.setRowCount(0);
                response.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
                log.warn("MCP 试跑执行失败(将被 LLM 修正): {}", response.getError());
            }
        } catch (BusinessException e) {
            // 安全插件拦截的错误
            response.setSuccess(false);
            response.setColumns(new ArrayList<>());
            response.setRows(new ArrayList<>());
            response.setRowCount(0);
            response.setError("SecurityServiceBlocked: " + e.getMessage());
            log.warn("MCP 试跑安全拦截: {}", response.getError());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setColumns(new ArrayList<>());
            response.setRows(new ArrayList<>());
            response.setRowCount(0);
            response.setError("UnknownError: " + e.getMessage());
        }
        
        return response;
    }
}
