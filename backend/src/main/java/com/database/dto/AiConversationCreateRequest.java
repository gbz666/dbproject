package com.database.dto;

import lombok.Data;

/**
 * 创建 AI 对话请求
 */
@Data
public class AiConversationCreateRequest {

    /**
     * 对话标题（可选，默认为"新对话"）
     */
    private String title;
}
