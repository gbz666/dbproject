package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「销项发票」Sheet 行 DTO
 * 只按中文表头名绑定，不处理英文表头
 */
@Data
public class ExcelSalesInvoiceRowDto {

    @ExcelProperty("销售订单号")
    private String salesOrderCode;

    @ExcelProperty("开票时间")
    private String invoiceDate;

    @ExcelProperty("公司名称")
    private String companyName;

    @ExcelProperty("货物或者应税劳务，服务名称")
    private String itemName;

    @ExcelProperty("规格型号")
    private String specification;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("数量")
    private String quantity;

    @ExcelProperty("含税单价")
    private String unitPriceInclusiveTax;

    @ExcelProperty("含税总金额")
    private String amountInclusiveTax;

    @ExcelProperty("未税销售金额")
    private String amountExclusiveTax;

    @ExcelProperty("税额")
    private String taxAmount;

    @ExcelProperty("发票编码")
    private String invoiceNo;

    /** 行是否需要处理：销售订单号非空即可 */
    public boolean isValid() {
        return trim(salesOrderCode) != null;
    }

    public String resolveSalesOrderCode() { return trim(salesOrderCode); }
    public String resolveInvoiceDate() { return trim(invoiceDate); }
    public String resolveCompanyName() { return trim(companyName); }
    public String resolveItemName() { return trim(itemName); }
    public String resolveSpecification() { return trim(specification); }
    public String resolveUnit() { return trim(unit); }

    private static String trim(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}

