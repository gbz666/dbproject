package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 生成 SQL 请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateSqlRequest {
    @NotBlank(message = "问题不能为空")
    private String question;
}
