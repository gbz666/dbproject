package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
@Data
public class PurchaseOrderVO {
    private Long id;
    private String purchaseCode;
    private Date orderDate;
    private String supplierCode;
    private String supplierName;

    private String note;

    // 接收前端传来的货物列表
    private List<PurchaseOrderVO.OrderItemDTO> items;
    private BigDecimal totalAmount;
    @Data
    public static class OrderItemDTO {
        private Long orderId;
        private String productCode;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        //采购额
        private BigDecimal lineTotal;
    }
}
