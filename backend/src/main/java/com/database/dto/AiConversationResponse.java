package com.database.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * AI 对话响应（包含消息列表）
 */
@Data
public class AiConversationResponse {

    /**
     * 对话 ID
     */
    private Long id;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 消息列表（仅在查询单个对话详情时返回）
     */
    private List<AiMessageResponse> messages;
}
