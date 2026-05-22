package com.database.dto;

import lombok.Data;

import java.util.Date;

/**
 * AI 消息响应
 */
@Data
public class AiMessageResponse {

    /**
     * 消息 ID
     */
    private Long id;

    /**
     * 消息角色：user/assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * AI 返回的 SQL 结果
     */
    private Object sqlResult;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 当前用户对该消息对应 memory 的反馈状态：+1 / -1 / null（未反馈）
     */
    private Integer feedbackVote;
}
