package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 试跑 SQL 请求对象
 */
@Data
public class AiTrialExecuteRequest {

    /**
     * 待试跑的 SQL
     */
    @NotBlank(message = "SQL 不能为空")
    private String sql;
}
