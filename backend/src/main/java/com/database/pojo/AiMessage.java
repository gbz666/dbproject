package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * AI 消息表：存储对话中的每条消息
 * @TableName ai_messages
 */
@Data
public class AiMessage {

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属对话 ID
     */
    private Long conversationId;

    /**
     * 消息角色：user/assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * AI 返回的 SQL 结果（JSON）
     */
    private String sqlResult;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private Date createdAt;
}
