package com.database.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class StockInDTO {
    // 入库单主表 ID (修改时必填，创建时为 null)
    private Long id;

    // 来源采购订单业务编号 (对应 purchase_orders.purchase_code)
    private String purchaseOrderCode;

    // 入库日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date stockInDate;

    // 入库备注
    private String note;

    // 入库明细列表
    private List<StockInItemDTO> items;

    @Data
    public static class StockInItemDTO {
        // 产品业务编号 (用于查询 products.id)
        private String productCode;

        // 序列号列表 (JSON 格式存储)
        private String serialNumbers;

        // 备注
        private String remark;

        // 该产品分配到不同仓库的明细
        // 复用你已有的 WarehouseStockVO (包含 warehouseId 和 quantity)
        private List<com.database.vo.WarehouseStockVO> warehouseDetails;
    }
}