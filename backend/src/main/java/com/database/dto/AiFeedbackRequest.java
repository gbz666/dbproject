package com.database.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 反馈请求：对某条 ai_sql_memory 投票（点赞 +1 / 点踩 -1）
 */
@Data
public class AiFeedbackRequest {

    @NotNull(message = "memoryId 不能为空")
    private Long memoryId;

    /** +1 表示点赞，-1 表示点踩 */
    @NotNull(message = "vote 不能为空")
    private Integer vote;
}
