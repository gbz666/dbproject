package com.database.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentExpenseDTO {
    private Long id;

    @NotBlank(message = "付款单号不能为空")
    private String paymentNo;

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    private Long purchaseInvoiceId;

    @NotNull(message = "付款金额不能为空")
    @DecimalMin(value = "0.01", message = "付款金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "付款日期不能为空")
    private LocalDate paymentDate;

    @NotBlank(message = "付款方式不能为空")
    private String method;

    private String remark;
}
