package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OutboundDetailVO {
    private Long id;
    private String salesOrderCode;      // 销售订单号
    private LocalDate orderDate;        // 订单日期
    private String customerName;        // 客户名称
    private String productCode;         // 产品型号
    private BigDecimal orderQuantity;   // 对应订货数
    private String orderRemark;         // 原销售订单备注
    private LocalDate firstOutboundDate; // 初次出库日期 (取该订单该产品最早的一次出库日期)

    // 满足你“仓库出货数量应该是 list”的要求
    private List<WarehouseStockVO> warehouseDetails;

    private BigDecimal subTotal;        // 出库小计 (计算属性)
    private BigDecimal pendingQuantity;  // 未交货数量 (订货数 - 小计)
    private String serialNumbers;       // 汇总后的序列号
}