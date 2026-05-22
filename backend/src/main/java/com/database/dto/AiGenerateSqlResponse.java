package com.database.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 生成 SQL 响应 DTO（对应 Python /generate-sql 返回）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateSqlResponse {

    private String sqlTemplate;
    private List<ParamSpec> paramsSpec;
    private String reason;
    private Map<String, Object> chartHint;
    private Double confidence;
    private List<String> warnings;
    /** ai_sql_memory 主键，前端执行成功后或点赞/踩时回传 */
    private Long memoryId;

    /**
     * 参数规格，描述 SQL 模板中的占位符参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamSpec {
        private String name;
        private String type;
        @JsonProperty("default")
        private Object defaultValue;
        private Boolean required;
        private String label;
    }
}
