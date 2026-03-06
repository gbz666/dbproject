package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class ProductRequest {
    /** 【重要】更新时必须传入ID，创建时为空 */
    private Long id;

    private String productName;
    private String categoryName; // 分类名称
    private String sku;
    private String unit;
    private String specification;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal listPrice;
    // ... 其他可能需要更新的字段
}