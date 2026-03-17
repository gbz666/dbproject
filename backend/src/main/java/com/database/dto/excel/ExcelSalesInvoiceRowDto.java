package com.database.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel「销项发票」Sheet 行 DTO（Sheet 名：销项）
 * 说明：
 * - 左侧是订单及统计汇总字段
 * - 右侧是实际每一条发票明细字段（导入时只依赖明细区）
 * - 「计数」列为辅助统计列，导入时完全忽略
 */
@Data
public class ExcelSalesInvoiceRowDto {

    // ====== 左侧：订单及统计汇总字段（目前仅作占位映射，不参与导入逻辑） ======

    @ExcelProperty("销售订单号")
    private String salesOrderCodeLeft;       // 左边订单信息区的销售订单号

    @ExcelProperty("订单日期")
    private String orderDate;

    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("产品型号")
    private String productModel;

    @ExcelProperty("订货数量")
    private String orderQuantity;

    @ExcelProperty("销售单价")
    private String salesUnitPrice;

    @ExcelProperty("销售金额")
    private String salesAmount;

    @ExcelProperty("备注")
    private String orderRemark;

    @ExcelProperty("累计开票日期")
    private String totalInvoiceDate;

    @ExcelProperty("平均开票时间")
    private String avgInvoiceDaysSummary;

    @ExcelProperty("累计开票金额")
    private String totalInvoiceAmount;

    @ExcelProperty("累计开票次数")
    private String totalInvoiceCount;

    @ExcelProperty("未开金额")
    private String pendingInvoiceAmountSummary;

    /** 计数列：仅供 Excel 统计使用，导入时不使用 */
    @ExcelProperty("计数")
    private String unusedCounter;

    // ====== 右侧：实际销项发票明细字段（导入逻辑只依赖这一块） ======

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

