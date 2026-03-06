package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「供应商」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 */
@Data
public class ExcelSupplierRowDto {

    @ExcelProperty("fuu")
    private String fuu;

    @ExcelProperty("供应商编码")
    private String supplierCode;

    @ExcelProperty("supplier_code")
    private String supplierCodeEn;

    @ExcelProperty("供应商全称(supplier)")
    private String supplierFullName;

    @ExcelProperty("供应商名称")
    private String supplierName;

    @ExcelProperty("supplier_name")
    private String supplierNameEn;

    @ExcelProperty("主营业务")
    private String mainBusiness;

    @ExcelProperty("main_business")
    private String mainBusinessEn;

    @ExcelProperty("销售")
    private String sales;

    @ExcelProperty("业绩")
    private String owner;

    @ExcelProperty("联系人1")
    private String contact1;

    @ExcelProperty("联系人2")
    private String contact2;

    @ExcelProperty("联系人3")
    private String contact3;

    public String resolveSupplierCode() {
        return firstNonEmpty(fuu, supplierCode, supplierCodeEn);
    }

    public String resolveSupplierName() {
        return firstNonEmpty(supplierFullName, supplierName, supplierNameEn);
    }

    public String resolveMainBusiness() {
        return firstNonEmpty(mainBusiness, mainBusinessEn);
    }

    public boolean isValid() {
        return resolveSupplierName() != null && !resolveSupplierName().isEmpty();
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
