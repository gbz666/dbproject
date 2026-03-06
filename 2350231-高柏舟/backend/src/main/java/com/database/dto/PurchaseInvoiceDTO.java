package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

@Data
public class PurchaseInvoiceDTO {
    private String purchaseCode;      // 采购订单号
    private String invoiceNo;         // 发票号
    private LocalDate invoiceDate;    // 开票/付款日期
    private String itemName;          // 货物或应税劳务名称
    private String specification;     // 规格型号 (对应 VO 的产品型号)
    private String unit;              // 单位
    private BigDecimal quantity;      // 数量
    private BigDecimal unitPrice;     // 单价
    private BigDecimal amountExclusiveTax; // 未税金额
    private BigDecimal taxRate;       // 税率
    private BigDecimal taxAmount;     // 税额
    private BigDecimal amountInclusiveTax; // 含税总金额
    private String remark;            // 备注
}