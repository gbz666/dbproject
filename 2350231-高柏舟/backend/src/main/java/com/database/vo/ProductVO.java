package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String productCode;
    private String productName;
    private String categoryName; // 联表查询结果
    private String sku;
    private String unit;
    private String specification;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal listPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}