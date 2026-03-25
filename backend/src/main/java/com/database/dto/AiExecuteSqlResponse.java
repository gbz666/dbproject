package com.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 执行 SQL 响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiExecuteSqlResponse {

    private List<String> columns;
    private List<List<Object>> rows;
    private String chartUrl;
    private String sqlTemplate;
    private Map<String, Object> params;
}
