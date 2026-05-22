package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * AI SQL 反馈表：记录用户对 SQL 模板的点赞/点踩
 * @TableName ai_sql_feedback
 */
@Data
public class AiSqlFeedback {

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联 ai_sql_memory.id
     */
    private Long memoryId;

    /**
     * 反馈用户（staffs.id）
     */
    private Long userId;

    /**
     * +1 表示赞，-1 表示踩
     */
    private Integer vote;

    /**
     * 反馈时间
     */
    private Date createdAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        AiSqlFeedback other = (AiSqlFeedback) that;
        return (this.id == null ? other.id == null : this.id.equals(other.id))
            && (this.memoryId == null ? other.memoryId == null : this.memoryId.equals(other.memoryId))
            && (this.userId == null ? other.userId == null : this.userId.equals(other.userId))
            && (this.vote == null ? other.vote == null : this.vote.equals(other.vote))
            && (this.createdAt == null ? other.createdAt == null : this.createdAt.equals(other.createdAt));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((memoryId == null) ? 0 : memoryId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((vote == null) ? 0 : vote.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", memoryId=").append(memoryId);
        sb.append(", userId=").append(userId);
        sb.append(", vote=").append(vote);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}
