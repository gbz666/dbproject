package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelPaymentRowDto {

    @ExcelProperty(value = "采购订单号", index = 15)
    private String purchaseCode;

    @ExcelProperty(value = "付款日期", index = 17)
    private String paymentDate;

    @ExcelProperty(value = "付出金额", index = 18)
    private String amount;

    @ExcelProperty(index = 19)
    private String methodOrRemark;

    public boolean isValid() {
        return trim(purchaseCode) != null;
    }

    public String resolvePurchaseCode() { return trim(purchaseCode); }
    public String resolvePaymentDate() { return trim(paymentDate); }
    public String resolveMethodOrRemark() { return trim(methodOrRemark); }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
