package com.database.dto;

import lombok.Data;

@Data
public class PaymentReceiptQuery {
    private int pageNum = 1;
    private int pageSize = 10;
    private String customerName;
    private String receiptNo;
    private String method;
}
