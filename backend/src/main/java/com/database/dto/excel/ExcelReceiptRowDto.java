package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelReceiptRowDto {

    @ExcelProperty(value = "销售订单号", index = 15)
    private String salesOrderCode;

    @ExcelProperty(value = "客户名称", index = 16)
    private String customerName;

    @ExcelProperty(value = "收款时间", index = 17)
    private String receiptDate;

    @ExcelProperty(value = "收款金额", index = 18)
    private String amount;

    @ExcelProperty(index = 19)
    private String remark;

    public boolean isValid() {
        return trim(salesOrderCode) != null;
    }

    public String resolveSalesOrderCode() { return trim(salesOrderCode); }
    public String resolveCustomerName() { return trim(customerName); }
    public String resolveReceiptDate() { return trim(receiptDate); }
    public String resolveRemark() { return trim(remark); }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
