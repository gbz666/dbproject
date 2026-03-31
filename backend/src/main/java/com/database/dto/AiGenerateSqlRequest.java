package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 生成 SQL 请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateSqlRequest {
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 对话历史，每条包含 role("user"/"assistant") 和 content
     */
    private List<Map<String, String>> history;
}
