package com.database.dto;

import lombok.Data;

@Data
public class PaymentExpenseQuery {
    private int pageNum = 1;
    private int pageSize = 10;
    private String supplierName;
    private String paymentNo;
    private String method;
}
