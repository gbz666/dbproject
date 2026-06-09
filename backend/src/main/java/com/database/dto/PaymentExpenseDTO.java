package com.database.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentExpenseDTO {
    private Long id;
    private String paymentNo;
    private Long supplierId;
    private Long purchaseInvoiceId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private String remark;
}
