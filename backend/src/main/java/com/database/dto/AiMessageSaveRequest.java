package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存 AI 消息请求
 */
@Data
public class AiMessageSaveRequest {

    /**
     * 消息角色：user/assistant
     */
    @NotBlank(message = "角色不能为空")
    private String role;

    /**
     * 消息内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * AI 返回的 SQL 结果（JSON 字符串）
     */
    private String sqlResult;

    /**
     * 错误信息
     */
    private String errorMsg;
}
