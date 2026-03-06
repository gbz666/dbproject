package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SalesOrderVO {
    private Long id;
    private String orderCode;
    private Date orderDate;
    private String customerCode;
    private String customerName;
    private String followUpPersonName;
    private String salesPersonName;
    private String ownerName;

    private String note;

    // 接收前端传来的货物列表
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long orderId;
        private String productCode;
        private String ProductName;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal costPrice;
        //销售额
        private Long salesCount;
        //采购小计
        private Long salesTotalCount;
    }

}
