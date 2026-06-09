package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentExpenseVO {
    private Long id;
    private String paymentNo;
    private Long supplierId;
    private String supplierName;
    private Long purchaseInvoiceId;
    private String purchaseInvoiceNo;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
