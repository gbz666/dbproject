package com.database.dto;

import lombok.Data;

@Data
public class PurchaseInvoiceQuery {
    private int pageNum = 1;
    private int pageSize = 10;
    private String supplierName;   // 供应商名称查询
    private String productModel;   // 产品型号查询 (对应 specification)
    private String purchaseCode;   // 采购订单号查询
    private String itemName;       // 货物/服务名称查询
}