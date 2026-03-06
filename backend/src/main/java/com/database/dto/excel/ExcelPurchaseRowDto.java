package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「采购」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 */
@Data
public class ExcelPurchaseRowDto {

    @ExcelProperty("采购号")
    private String purchaseCode;

    @ExcelProperty("采购订单号")
    private String purchaseOrderCode;

    @ExcelProperty("purchase_code")
    private String purchaseCodeEn;

    @ExcelProperty("供应商编码")
    private String supplierCode;

    @ExcelProperty("supplier_code")
    private String supplierCodeEn;

    @ExcelProperty("产品编码")
    private String productCode;

    @ExcelProperty("product_code")
    private String productCodeEn;

    @ExcelProperty("数量")
    private String quantity;

    @ExcelProperty("采购单价")
    private String unitPrice;

    @ExcelProperty("单价")
    private String unitPriceAlt;

    @ExcelProperty("定货日期")
    private String orderDate;

    @ExcelProperty("订单日期")
    private String orderDateAlt;

    @ExcelProperty("order_date")
    private String orderDateEn;

    @ExcelProperty("备注")
    private String remark;

    public String resolvePurchaseCode() {
        return firstNonEmpty(purchaseCode, purchaseOrderCode, purchaseCodeEn);
    }

    public String resolveSupplierCode() {
        return firstNonEmpty(supplierCode, supplierCodeEn);
    }

    public String resolveProductCode() {
        return firstNonEmpty(productCode, productCodeEn);
    }

    public String resolveOrderDate() {
        return firstNonEmpty(orderDate, orderDateAlt, orderDateEn);
    }

    public boolean isValid() {
        // 只要有采购单号，就认为这一行需要处理；
        // 供应商缺失的情况由业务层兜底到“虚拟供应商”
        return resolvePurchaseCode() != null && !resolvePurchaseCode().isEmpty();
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
