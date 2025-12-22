package com.database.vo;

import com.database.dto.PurchaseInvoiceDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseInvoiceVO extends PurchaseInvoiceDTO {
    private Long id;
    private LocalDate orderDate;      // 订单日期
    private String supplierName;      // 供应商名称
    private String orderNote;         // 订单备注

    // 统计字段
    private LocalDate lastInvoiceDate; // 累计进项日期(最近一次)
    private Double avgInvoiceDays;     // 平均进项时间(与订单日期间隔)
    private BigDecimal totalInvoicedAmount; // 累计进项金额
    private Integer invoiceCount;      // 累计进项次数
    private BigDecimal pendingInvoiceAmount; // 缺进项金额 (订单总额 - 已开票总额)
}