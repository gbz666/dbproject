package com.database.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentReconciliationVO {
    // 采购基础信息
    private String purchaseCode;      // 采购订单号
    private LocalDate orderDate;      // 订单日期
    private String supplierName;      // 供应商名称
    private String specification;     // 产品型号
    private BigDecimal quantity;      // 数量
    private BigDecimal unitPrice;     // 采购单价
    private BigDecimal lineTotal;     // 采购额
    private String orderNote;         // 订单备注

    // 统计维度
    private LocalDate lastPaymentDate; // 累计付款日期 (取最后一次)
    private Double avgPaymentDays;    // 平均付款时间 (天)
    private BigDecimal totalPaidAmount; // 累计付款金额
    private Integer paymentCount;     // 累计付款次数
    private BigDecimal unpaidAmount;  // 未付款金额

    // 录入项 (用于当前操作)
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String remark;
}