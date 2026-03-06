package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「出库」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 */
@Data
public class ExcelOutboundRowDto {

    @ExcelProperty("销售订单号")
    private String orderCode;

    @ExcelProperty("order_code")
    private String orderCodeEn;

    @ExcelProperty("产品型号")
    private String productModel;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("customer_name")
    private String customerNameEn;

    @ExcelProperty("订单日期")
    private String orderDate;

    @ExcelProperty("原销售订单备注")
    private String note;

    @ExcelProperty("初次出库日期")
    private String outboundDate;

    @ExcelProperty("对应订货数")
    private String orderQty;

    @ExcelProperty("上海仓出货数量")
    private String qtyShanghai;

    @ExcelProperty("天津仓出货数量")
    private String qtyTianjin;

    @ExcelProperty("深圳仓出库数")
    private String qtyShenzhen;

    public String resolveOrderCode() {
        return firstNonEmpty(orderCode, orderCodeEn);
    }

    public String resolveProductName() {
        return firstNonEmpty(productModel, productName);
    }

    public String resolveCustomerName() {
        return firstNonEmpty(customerName, customerNameEn);
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
