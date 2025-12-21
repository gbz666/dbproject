package com.database.vo;


import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class StockInVO {
    // 入库单 ID
    private Long id;

    // 入库单业务编号
    private String stockInCode;

    // 关联的采购单编号
    private String purchaseOrderCode;

    // 供应商名称 (来自 suppliers 表)
    private String supplierName;

    // 入库日期
    private Date orderDate;

    // 产品编号及名称
    private String productCode;
    private String productName;

    // 采购订单中的原始数量
    private BigDecimal purchaseQuantity;

    // 本次入库的所有仓库合计数量
    private BigDecimal subTotal;

    // 剩余未入库数量 (purchaseQuantity - subTotal)
    private BigDecimal pendingQuantity;

    // 入库备注
    private String note;

    // 操作人姓名 (关联 staffs 表)
    private String creatorName;

    // 具体的仓库分配详情
    private List<WarehouseStockVO> warehouseDetails;
}