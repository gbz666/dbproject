package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「销售」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 */
@Data
public class ExcelSalesRowDto {

    @ExcelProperty("舟若订单号")
    private String orderCode;

    @ExcelProperty("销售订单号")
    private String orderCodeAlt;

    @ExcelProperty("order_code")
    private String orderCodeEn;

    @ExcelProperty("客户编码")
    private String customerCode;

    @ExcelProperty("customer_code")
    private String customerCodeEn;

    @ExcelProperty("产品编码")
    private String productCode;

    @ExcelProperty("product_code")
    private String productCodeEn;

    @ExcelProperty("数量")
    private String quantity;

    @ExcelProperty("销售单价")
    private String unitPrice;

    @ExcelProperty("销售单价 ")  // 表头带尾随空格时仍能匹配
    private String unitPriceTrailingSpace;

    @ExcelProperty("单价")
    private String unitPriceAlt;

    @ExcelProperty("成本单价")
    private String costPrice;

    @ExcelProperty("成本单价 ")
    private String costPriceTrailingSpace;

    @ExcelProperty("成本价")
    private String costPriceAlt;

    @ExcelProperty("订单日期")
    private String orderDate;

    @ExcelProperty("order_date")
    private String orderDateEn;

    @ExcelProperty("跟单人员（从AS列选择）")
    private String followUp;

    @ExcelProperty("跟单")
    private String followUpAlt;

    @ExcelProperty("follow_up")
    private String followUpEn;

    @ExcelProperty("备注栏")
    private String remark;

    @ExcelProperty("备注")
    private String remarkAlt;

    public String resolveOrderCode() {
        return firstNonEmpty(orderCode, orderCodeAlt, orderCodeEn);
    }

    public String resolveCustomerCode() {
        return firstNonEmpty(customerCode, customerCodeEn);
    }

    public String resolveProductCode() {
        return firstNonEmpty(productCode, productCodeEn);
    }

    /** 销售单价：优先 Excel 中的值，兼容表头带空格、别名「单价」 */
    public String resolveUnitPrice() {
        return firstNonEmpty(unitPrice, unitPriceTrailingSpace, unitPriceAlt);
    }

    /** 成本单价：优先 Excel 中的值，兼容表头带空格、别名「成本价」 */
    public String resolveCostPrice() {
        return firstNonEmpty(costPrice, costPriceTrailingSpace, costPriceAlt);
    }

    public String resolveOrderDate() {
        return firstNonEmpty(orderDate, orderDateEn);
    }

    public String resolveFollowUp() {
        return firstNonEmpty(followUp, followUpAlt, followUpEn);
    }

    public String resolveRemark() {
        return firstNonEmpty(remark, remarkAlt);
    }

    public boolean isValid() {
        return resolveOrderCode() != null && !resolveOrderCode().isEmpty();
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
