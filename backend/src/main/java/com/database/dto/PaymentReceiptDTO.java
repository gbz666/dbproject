package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentReceiptDTO {
    private Long id;
    private String receiptNo;
    private Long customerId;
    private Long salesInvoiceId;
    private BigDecimal amount;
    private LocalDate receiptDate;
    private String method;
    private String remark;
}
