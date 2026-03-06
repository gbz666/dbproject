package com.database.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class PurchaseOrderDto {
    private Long id; // 修改时使用
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderDate;
    private String supplierCode;
    private int supplierId;
    private String note;

    // 货物明细列表
    private List<PurchaseOrderDto.ItemDTO> items;

    @Data
    public static class ItemDTO {
        private String productCode;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String remark;
        // 成本价由后端自动获取，不需要前端传
    }
}
