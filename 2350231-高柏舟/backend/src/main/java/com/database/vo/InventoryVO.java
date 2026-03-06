package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryVO {
    private Long productId;
    private String productCode;
    private String productName;
    private String specification;
    private String unit;
    private BigDecimal costPrice;
    // 平铺各仓库库存
    private BigDecimal shInventory = BigDecimal.ZERO; // 上海
    private BigDecimal tjInventory = BigDecimal.ZERO; // 天津
    private BigDecimal szInventory = BigDecimal.ZERO; // 深圳
    private BigDecimal totalInventory = BigDecimal.ZERO;
}