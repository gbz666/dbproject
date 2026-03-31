package com.database.service;

import com.database.dto.AiExecuteSqlRequest;
import com.database.dto.AiExecuteSqlResponse;
import com.database.dto.AiGenerateSqlRequest;
import com.database.dto.AiGenerateSqlResponse;
import com.database.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    @Value("${aiagent.base-url:http://localhost:8001}")
    private String aiagentBaseUrl;

    @Value("${aiagent.sql.max-rows:100}")
    private int maxRows;

    @Value("${aiagent.sql.timeout-ms:15000}")
    private int timeoutMs;

    /**
     * 调用 Python 服务生成参数化 SQL 模板
     */
    public AiGenerateSqlResponse generateSql(AiGenerateSqlRequest request) {
        String url = aiagentBaseUrl + "/generate-sql";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("question", request.getQuestion());
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            body.put("history", request.getHistory());
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<AiGenerateSqlResponse> resp = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AiGenerateSqlResponse.class
            );
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
            throw new BusinessException("AI 服务返回异常", 502);
        } catch (RestClientException e) {
            log.error("调用 Python AI 服务失败: {}", e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试", 503);
        }
    }

    /**
     * 安全执行 SQL：参数替换 -> 安全校验 -> EXPLAIN 校验 -> 执行 -> 调图表服务
     */
    public AiExecuteSqlResponse executeSql(AiExecuteSqlRequest request, String question) {
        String sqlTemplate = request.getSqlTemplate();
        Map<String, Object> params = request.getParams();

        String finalSql = sqlSecurityService.renderSql(sqlTemplate, params);
        finalSql = sqlSecurityService.ensureLimit(finalSql);

        sqlSecurityService.validateSql(finalSql);

        runExplainCheck(finalSql);

        long start = System.currentTimeMillis();
        List<Map<String, Object>> resultMaps;
        try {
            jdbcTemplate.setQueryTimeout(timeoutMs / 1000);
            resultMaps = jdbcTemplate.queryForList(finalSql);
        } catch (Exception e) {
            log.error("AI SQL 执行失败: {}", e.getMessage(), e);
            throw new BusinessException("SQL 执行失败: " + e.getMessage(), 400);
        }
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

        AiExecuteSqlResponse response = new AiExecuteSqlResponse();
        response.setColumns(columns);
        response.setRows(rows);
        response.setChartUrl(chartUrl);
        response.setSqlTemplate(sqlTemplate);
        response.setParams(params);
        return response;
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
}
