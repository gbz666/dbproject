package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI 执行 SQL 请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiExecuteSqlRequest {

    @NotBlank(message = "SQL 模板不能为空")
    private String sqlTemplate;

    private Map<String, Object> params;

    private Map<String, Object> chartHint;

    private String question;

    /** ai_sql_memory 主键，由 generate-sql 透传过来用于关联审计与统计 */
    private Long memoryId;
}
