package com.database.vo;

import com.database.pojo.PurchaseInvoiceDetails;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 进项发票返回 VO：列表时带首行明细字段，按 ID 查询时带完整 details
 */
@Data
public class PurchaseInvoiceVO {
    private Long id;
    private String purchaseCode;
    private String invoiceNo;
    private LocalDate invoiceDate;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private String orderNote;
    private String remark;

    /** 列表展示用：发票全貌（总金额、明细数） */
    private BigDecimal totalAmount;   // 发票含税总金额
    private Integer detailCount;      // 明细行数（共几项）

    /** 按 ID 查单张时由 buildVOWithDetails 填充首行明细，用于兼容 */
    private String itemName;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amountExclusiveTax;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountInclusiveTax;

    /** 统计字段 */
    private LocalDate lastInvoiceDate;
    private Double avgInvoiceDays;
    private BigDecimal totalInvoicedAmount;
    private Integer invoiceCount;
    private BigDecimal pendingInvoiceAmount;

    /** 按 ID 查询时的完整明细列表 */
    private List<PurchaseInvoiceDetails> details;
}
