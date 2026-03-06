package com.database.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 进项发票创建/更新 DTO，一张发票可包含多行货物明细
 */
@Data
public class PurchaseInvoiceDTO {
    private String purchaseCode;       // 采购订单号
    private String invoiceNo;          // 发票号
    private LocalDate invoiceDate;      // 开票日期
    private Long supplierId;            // 供应商ID（可选，可由采购订单带出）
    private String remark;              // 发票备注
    /** 发票明细行，至少一行 */
    private List<PurchaseInvoiceDetailItemDTO> items;
}
