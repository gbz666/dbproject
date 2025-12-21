package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WarehouseStockVO {
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal quantity;
}