package com.database.dto;

import lombok.Data;

/**
 * 销项发票分页查询条件
 */
@Data
public class SalesInvoiceQuery {

    /**
     * 页码
     */
    private int pageNum = 1;

    /**
     * 每页条数
     */
    private int pageSize = 10;

    /**
     * 公司名称 / 客户名称
     */
    private String companyName;

    /**
     * 产品型号（对应 specification）
     */
    private String productModel;

    /**
     * 销售订单号
     */
    private String salesOrderCode;

    /**
     * 货物/服务名称
     */
    private String itemName;

    /**
     * 发票编码
     */
    private String invoiceNo;
}

