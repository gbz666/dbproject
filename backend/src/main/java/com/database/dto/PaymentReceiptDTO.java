package com.database.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentReceiptDTO {
    private Long id;

    @NotBlank(message = "收款单号不能为空")
    private String receiptNo;

    @NotNull(message = "客户不能为空")
    private Long customerId;

    private Long salesInvoiceId;

    @NotNull(message = "收款金额不能为空")
    @DecimalMin(value = "0.01", message = "收款金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "收款日期不能为空")
    private LocalDate receiptDate;

    @NotBlank(message = "收款方式不能为空")
    private String method;

    private String remark;
}
