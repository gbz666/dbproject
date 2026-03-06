package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「入库」Sheet 行 DTO，使用 EasyExcel 注解按表头绑定；列可部分存在。
 * 仓库数量列：入上海仓库/上海仓/上海、入天津仓库/天津仓/天津、入深圳仓库/深圳仓/深圳。
 */
@Data
public class ExcelStockInRowDto {

    @ExcelProperty("采购订单号")
    private String purchaseOrderCode;

    @ExcelProperty("purchase_order_code")
    private String purchaseOrderCodeEn;

    @ExcelProperty("初次入库日期")
    private String stockInDate;

    @ExcelProperty("入库日期")
    private String stockInDateAlt;

    @ExcelProperty("入上海仓库")
    private String qtyShanghai;

    @ExcelProperty("上海仓")
    private String qtyShanghaiAlt;

    @ExcelProperty("上海")
    private String qtyShanghaiAlt2;

    @ExcelProperty("入天津仓库")
    private String qtyTianjin;

    @ExcelProperty("天津仓")
    private String qtyTianjinAlt;

    @ExcelProperty("天津")
    private String qtyTianjinAlt2;

    @ExcelProperty("入深圳仓库")
    private String qtyShenzhen;

    @ExcelProperty("深圳仓")
    private String qtyShenzhenAlt;

    @ExcelProperty("深圳")
    private String qtyShenzhenAlt2;

    public String resolvePurchaseOrderCode() {
        return firstNonEmpty(purchaseOrderCode, purchaseOrderCodeEn);
    }

    public String resolveStockInDate() {
        return firstNonEmpty(stockInDate, stockInDateAlt);
    }

    public String resolveQtyShanghai() {
        return firstNonEmpty(qtyShanghai, qtyShanghaiAlt, qtyShanghaiAlt2);
    }

    public String resolveQtyTianjin() {
        return firstNonEmpty(qtyTianjin, qtyTianjinAlt, qtyTianjinAlt2);
    }

    public String resolveQtyShenzhen() {
        return firstNonEmpty(qtyShenzhen, qtyShenzhenAlt, qtyShenzhenAlt2);
    }

    public boolean isValid() {
        return resolvePurchaseOrderCode() != null && !resolvePurchaseOrderCode().isEmpty();
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }
}
