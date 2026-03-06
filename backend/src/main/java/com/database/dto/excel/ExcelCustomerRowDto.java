package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「客户」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 */
@Data
public class ExcelCustomerRowDto {

    @ExcelProperty("客户编码")
    private String customerCode;

    @ExcelProperty("customer_code")
    private String customerCodeEn;

    @ExcelProperty("客户名称(customer)")
    private String customerNameFull;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("customer_name")
    private String customerNameEn;

    @ExcelProperty("账期天数")
    private String paymentTermsDays;

    @ExcelProperty("账期备注")
    private String paymentTermsNotes;

    @ExcelProperty("销售")
    private String sales;

    @ExcelProperty("跟单")
    private String followUp;

    @ExcelProperty("业绩")
    private String owner;

    public String resolveCustomerCode() {
        return firstNonEmpty(customerCode, customerCodeEn);
    }

    public String resolveCustomerName() {
        return firstNonEmpty(customerNameFull, customerName, customerNameEn);
    }

    public boolean isValid() {
        String code = resolveCustomerCode();
        String name = resolveCustomerName();
        return code != null && !code.isEmpty() && name != null && !name.isEmpty();
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
