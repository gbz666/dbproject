package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新 AI 对话请求
 */
@Data
public class AiConversationUpdateRequest {

    /**
     * 新的对话标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
}
