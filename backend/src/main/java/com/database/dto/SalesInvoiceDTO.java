package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销项发票录入 DTO（对应外部填写表格的字段）
 */
@Data
public class SalesInvoiceDTO {

    /**
     * 销售订单号
     */
    private String salesOrderCode;

    /**
     * 开票时间
     */
    private LocalDate invoiceDate;

    /**
     * 公司名称（客户名称）
     */
    private String companyName;

    /**
     * 货物或者应税劳务、服务名称
     */
    private String itemName;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 含税单价
     */
    private BigDecimal unitPriceInclusiveTax;

    /**
     * 含税总金额
     */
    private BigDecimal amountInclusiveTax;

    /**
     * 未税销售金额
     */
    private BigDecimal amountExclusiveTax;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 发票编码
     */
    private String invoiceNo;

    /**
     * 备注（可选）
     */
    private String remark;
}

