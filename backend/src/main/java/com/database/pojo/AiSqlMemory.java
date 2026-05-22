package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * AI SQL 模板记忆表：存储 LLM 生成的参数化 SQL，供历史检索复用
 * @TableName ai_sql_memory
 */
@Data
public class AiSqlMemory {

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户原始自然语言问题
     */
    private String questionText;

    /**
     * 归一化问题（小写、去停用词，用于检索匹配）
     */
    private String normalizedQuestion;

    /**
     * 参数化 SQL 模板（含 {param} 占位符）
     */
    private String sqlTemplate;

    /**
     * 参数规格 JSON 数组 [{name,type,default,required,label}]
     */
    private String paramsSpec;

    /**
     * 涉及的表名（逗号分隔，便于表重叠度计算）
     */
    private String tablesUsed;

    /**
     * 意图标签（ranking/trend/proportion/summary/detail）
     */
    private String intentTag;

    /**
     * 图表建议 {type,x,y,series}
     */
    private String chartHint;

    /**
     * LLM 生成时的置信度（0~1，DECIMAL(4,3)）
     */
    private BigDecimal confidence;

    /**
     * 被复用次数
     */
    private Integer useCount;

    /**
     * 执行成功次数
     */
    private Integer successCount;

    /**
     * 最近一次被使用的时间
     */
    private Date lastUsedAt;

    /**
     * 模板版本号（每次改写自增）
     */
    private Integer version;

    /**
     * 状态：draft / published / archived
     */
    private String status;

    /**
     * 创建人（staffs.id）
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        AiSqlMemory other = (AiSqlMemory) that;
        return (this.id == null ? other.id == null : this.id.equals(other.id))
            && (this.questionText == null ? other.questionText == null : this.questionText.equals(other.questionText))
            && (this.normalizedQuestion == null ? other.normalizedQuestion == null : this.normalizedQuestion.equals(other.normalizedQuestion))
            && (this.sqlTemplate == null ? other.sqlTemplate == null : this.sqlTemplate.equals(other.sqlTemplate))
            && (this.paramsSpec == null ? other.paramsSpec == null : this.paramsSpec.equals(other.paramsSpec))
            && (this.tablesUsed == null ? other.tablesUsed == null : this.tablesUsed.equals(other.tablesUsed))
            && (this.intentTag == null ? other.intentTag == null : this.intentTag.equals(other.intentTag))
            && (this.chartHint == null ? other.chartHint == null : this.chartHint.equals(other.chartHint))
            && (this.confidence == null ? other.confidence == null : this.confidence.equals(other.confidence))
            && (this.useCount == null ? other.useCount == null : this.useCount.equals(other.useCount))
            && (this.successCount == null ? other.successCount == null : this.successCount.equals(other.successCount))
            && (this.lastUsedAt == null ? other.lastUsedAt == null : this.lastUsedAt.equals(other.lastUsedAt))
            && (this.version == null ? other.version == null : this.version.equals(other.version))
            && (this.status == null ? other.status == null : this.status.equals(other.status))
            && (this.createdBy == null ? other.createdBy == null : this.createdBy.equals(other.createdBy))
            && (this.createdAt == null ? other.createdAt == null : this.createdAt.equals(other.createdAt))
            && (this.updatedAt == null ? other.updatedAt == null : this.updatedAt.equals(other.updatedAt));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((questionText == null) ? 0 : questionText.hashCode());
        result = prime * result + ((normalizedQuestion == null) ? 0 : normalizedQuestion.hashCode());
        result = prime * result + ((sqlTemplate == null) ? 0 : sqlTemplate.hashCode());
        result = prime * result + ((paramsSpec == null) ? 0 : paramsSpec.hashCode());
        result = prime * result + ((tablesUsed == null) ? 0 : tablesUsed.hashCode());
        result = prime * result + ((intentTag == null) ? 0 : intentTag.hashCode());
        result = prime * result + ((chartHint == null) ? 0 : chartHint.hashCode());
        result = prime * result + ((confidence == null) ? 0 : confidence.hashCode());
        result = prime * result + ((useCount == null) ? 0 : useCount.hashCode());
        result = prime * result + ((successCount == null) ? 0 : successCount.hashCode());
        result = prime * result + ((lastUsedAt == null) ? 0 : lastUsedAt.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", questionText=").append(questionText);
        sb.append(", confidence=").append(confidence);
        sb.append(", useCount=").append(useCount);
        sb.append(", successCount=").append(successCount);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}
