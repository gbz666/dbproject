package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * AI SQL 执行日志表：记录每次 AI 接口的 SQL 执行详情，用于审计与统计
 * @TableName ai_sql_exec_log
 */
@Data
public class AiSqlExecLog {

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联 ai_sql_memory.id（首次生成时可为空）
     */
    private Long memoryId;

    /**
     * 执行人（staffs.id）
     */
    private Long userId;

    /**
     * SQL 文本 SHA-256 哈希
     */
    private String sqlHash;

    /**
     * 实际执行的完整 SQL（参数已替换）
     */
    private String sqlText;

    /**
     * 实际执行参数（JSON）
     */
    private String params;

    /**
     * 返回行数
     */
    private Integer rowCount;

    /**
     * 执行耗时（毫秒）
     */
    private Integer latencyMs;

    /**
     * 执行状态：success / explain_fail / security_reject / timeout / error
     */
    private String status;

    /**
     * 错误信息（仅失败时填写）
     */
    private String errorMsg;

    /**
     * 执行时间
     */
    private Date createdAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        AiSqlExecLog other = (AiSqlExecLog) that;
        return (this.id == null ? other.id == null : this.id.equals(other.id))
            && (this.memoryId == null ? other.memoryId == null : this.memoryId.equals(other.memoryId))
            && (this.userId == null ? other.userId == null : this.userId.equals(other.userId))
            && (this.sqlHash == null ? other.sqlHash == null : this.sqlHash.equals(other.sqlHash))
            && (this.sqlText == null ? other.sqlText == null : this.sqlText.equals(other.sqlText))
            && (this.params == null ? other.params == null : this.params.equals(other.params))
            && (this.rowCount == null ? other.rowCount == null : this.rowCount.equals(other.rowCount))
            && (this.latencyMs == null ? other.latencyMs == null : this.latencyMs.equals(other.latencyMs))
            && (this.status == null ? other.status == null : this.status.equals(other.status))
            && (this.errorMsg == null ? other.errorMsg == null : this.errorMsg.equals(other.errorMsg))
            && (this.createdAt == null ? other.createdAt == null : this.createdAt.equals(other.createdAt));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((memoryId == null) ? 0 : memoryId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((sqlHash == null) ? 0 : sqlHash.hashCode());
        result = prime * result + ((sqlText == null) ? 0 : sqlText.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
        result = prime * result + ((rowCount == null) ? 0 : rowCount.hashCode());
        result = prime * result + ((latencyMs == null) ? 0 : latencyMs.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
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
        sb.append(", sqlHash=").append(sqlHash);
        sb.append(", rowCount=").append(rowCount);
        sb.append(", latencyMs=").append(latencyMs);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}
