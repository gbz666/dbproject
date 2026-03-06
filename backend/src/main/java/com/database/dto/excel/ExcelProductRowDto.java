package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「产品」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 */
@Data
public class ExcelProductRowDto {

    @ExcelProperty("产品编码")
    private String productCode;

    @ExcelProperty("product_code")
    private String productCodeEn;

    @ExcelProperty("产品名称（1，卡类；2，固态硬盘；3，机械硬盘；4，内存；5，电源；6，线材 数据线；7，其它）")
    private String productName;

    @ExcelProperty("规格")
    private String spec;

    @ExcelProperty("卡类")
    private String categoryCol;

    @ExcelProperty("产品描述")
    private String description;

    @ExcelProperty("description")
    private String descriptionEn;

    /** 解析后的产品编码：优先 productCode，否则 productCodeEn */
    public String resolveProductCode() {
        return firstNonEmpty(productCode, productCodeEn);
    }

    /** 解析后的产品名称：产品名称 → 规格 → 卡类 → 产品描述 → 编码 */
    public String resolveProductName() {
        String name = firstNonEmpty(productName, spec, categoryCol, description, descriptionEn);
        return name != null && !name.isEmpty() ? name : resolveProductCode();
    }

    /** 解析后的描述 */
    public String resolveDescription() {
        return firstNonEmpty(description, descriptionEn);
    }

    public boolean isValid() {
        String code = resolveProductCode();
        return code != null && !code.isEmpty() && !"nan".equalsIgnoreCase(code);
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
