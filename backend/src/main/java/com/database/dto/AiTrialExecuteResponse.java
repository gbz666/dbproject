package com.database.dto;

import lombok.Data;
import java.util.List;

/**
 * AI 试跑 SQL 响应对象
 */
@Data
public class AiTrialExecuteResponse {
    
    private boolean success;
    
    private List<String> columns;
    
    private List<List<Object>> rows;
    
    private int rowCount;
    
    private String error;
}
