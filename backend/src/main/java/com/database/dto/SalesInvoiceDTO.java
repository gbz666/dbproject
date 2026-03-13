package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 销项发票录入 DTO（对应外部填写表格的字段）
 * 支持一票多明细：优先使用 items；若 items 为空则从扁平字段构建单条明细（兼容旧前端）
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
     * 发票编码
     */
    private String invoiceNo;

    /**
     * 备注（可选）
     */
    private String remark;

    /**
     * 发票明细行，至少一行。若为空则从下述扁平字段构建单条明细
     */
    private List<SalesInvoiceDetailItemDTO> items;

    // ------ 以下为扁平字段，兼容仅传单明细的旧前端 ------

    /** 货物或者应税劳务、服务名称 */
    private String itemName;
    /** 规格型号 */
    private String specification;
    /** 单位 */
    private String unit;
    /** 数量 */
    private BigDecimal quantity;
    /** 含税单价 */
    private BigDecimal unitPriceInclusiveTax;
    /** 含税总金额 */
    private BigDecimal amountInclusiveTax;
    /** 未税销售金额 */
    private BigDecimal amountExclusiveTax;
    /** 税额 */
    private BigDecimal taxAmount;
}

