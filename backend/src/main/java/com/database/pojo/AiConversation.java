package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * AI 对话表：存储用户的 AI 对话会话
 * @TableName ai_conversations
 */
@Data
public class AiConversation {

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属用户（staffs.id）
     */
    private Long userId;

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
     * 是否软删除:0=正常,1=已删除
     */
    private Integer isDeleted;

    /**
     * 删除时间
     */
    private Date deletedAt;
}
