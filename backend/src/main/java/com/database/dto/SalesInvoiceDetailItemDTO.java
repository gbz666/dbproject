package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销项发票单行明细（创建/更新时使用，一张发票可多行）
 */
@Data
public class SalesInvoiceDetailItemDTO {
    private String itemName;              // 货物或应税劳务、服务名称
    private String specification;         // 规格型号
    private String unit;                  // 单位
    private BigDecimal quantity;          // 数量
    private BigDecimal unitPriceInclusiveTax; // 含税单价
    private BigDecimal amountExclusiveTax;    // 未税金额
    private BigDecimal taxAmount;             // 税额
    private BigDecimal amountInclusiveTax;    // 含税金额
    private String remark;                    // 备注
}
