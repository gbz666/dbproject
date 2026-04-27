package com.database.controller;

import com.database.aop.RequireRole;
import com.database.dto.AiExecuteSqlRequest;
import com.database.dto.AiExecuteSqlResponse;
import com.database.dto.AiGenerateSqlRequest;
import com.database.dto.AiGenerateSqlResponse;
import com.database.service.AiAgentService;
import com.database.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI Agent 控制器：自然语言查询 SQL 生成与执行
 */
@RestController
@RequestMapping("/api/ai")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    @Autowired
    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    /**
     * POST /api/ai/generate-sql: 将自然语言问题转为参数化 SQL 模板
     * @param request 包含 question 字段
     * @return SQL 模板、参数规格、图表建议等
     */
    @PostMapping("/generate-sql")
    public ResponseEntity<Result<AiGenerateSqlResponse>> generateSql(
            @Valid @RequestBody AiGenerateSqlRequest request) {
        AiGenerateSqlResponse response = aiAgentService.generateSql(request);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * POST /api/ai/execute-sql: 安全执行参数化 SQL 并返回结果 + 图表
     * @param request 包含 sqlTemplate、params、chartHint、question
     * @return 查询列名、行数据、图表 URL
     */
    @RequireRole({"后台管理", "总经理"})
    @PostMapping("/execute-sql")
    public ResponseEntity<Result<AiExecuteSqlResponse>> executeSql(
            @Valid @RequestBody AiExecuteSqlRequest request) {
        AiExecuteSqlResponse response = aiAgentService.executeSql(request, request.getQuestion());
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * POST /api/ai/internal/trial-execute: 内部试跑 SQL，作为 MCP Tool Server 暴露给 AI Agent
     * @param request 包含 sql
     * @return 试跑结果
     */
    @RequireRole({"后台管理", "总经理"})
    @PostMapping("/internal/trial-execute")
    public ResponseEntity<com.database.dto.AiTrialExecuteResponse> trialExecute(
            @Valid @RequestBody com.database.dto.AiTrialExecuteRequest request) {
        com.database.dto.AiTrialExecuteResponse response = aiAgentService.trialExecute(request.getSql());
        return ResponseEntity.ok(response);
    }
}
