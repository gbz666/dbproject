package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「进项发票」Sheet 行 DTO
 * 只按中文表头名绑定，不处理英文表头
 */
@Data
public class ExcelPurchaseInvoiceRowDto {

    // Excel 从 1 开始的第 16 列 => EasyExcel index = 15
    @ExcelProperty(value = "采购订单号", index = 15)
    private String purchaseCode;

    @ExcelProperty("时间")
    private String invoiceDate;

    @ExcelProperty("公司名称")
    private String supplierName;

    @ExcelProperty("货物或者应税劳务，服务名称")
    private String itemName;

    @ExcelProperty("规格型号")
    private String specification;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty(value = "数量", index = 21)
    private String quantity;

    @ExcelProperty("单价")
    private String unitPrice;

    @ExcelProperty("未税金额")
    private String amountExclusiveTax;

    @ExcelProperty("税率")
    private String taxRate;

    @ExcelProperty("税额")
    private String taxAmount;

    @ExcelProperty("含税总金额")
    private String amountInclusiveTax;

    public boolean isValid() {
        return trim(purchaseCode) != null;
    }

    public String resolvePurchaseCode() { return trim(purchaseCode); }
    public String resolveInvoiceDate() { return trim(invoiceDate); }
    public String resolveSupplierName() { return trim(supplierName); }
    public String resolveItemName() { return trim(itemName); }
    public String resolveSpecification() { return trim(specification); }
    public String resolveUnit() { return trim(unit); }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}

