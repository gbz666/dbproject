package com.database.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SalesOrderDTO {
    private Long id; // 修改时使用
    private Date orderDate;
    private String customerCode;
    private String note;

    // 货物明细列表
    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private String productCode;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        // 成本价由后端自动获取，不需要前端传
    }
}