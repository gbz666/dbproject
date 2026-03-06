package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 进项发票单行明细（创建/更新时使用，一张发票可多行）
 */
@Data
public class PurchaseInvoiceDetailItemDTO {
    private String itemName;          // 货物或应税劳务名称
    private String specification;     // 规格型号
    private String unit;               // 单位
    private BigDecimal quantity;       // 数量
    private BigDecimal unitPrice;      // 单价
    private BigDecimal amountExclusiveTax; // 未税金额
    private BigDecimal taxRate;        // 税率
    private BigDecimal taxAmount;      // 税额
    private BigDecimal amountInclusiveTax; // 含税金额
    private String remark;             // 备注
}
