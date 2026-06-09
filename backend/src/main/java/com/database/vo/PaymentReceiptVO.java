package com.database.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentReceiptVO {
    private Long id;
    private String receiptNo;
    private Long customerId;
    private String customerName;
    private Long salesInvoiceId;
    private String salesInvoiceNo;
    private BigDecimal amount;
    private LocalDate receiptDate;
    private String method;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
