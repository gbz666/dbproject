package com.database.dto;

import com.database.vo.WarehouseStockVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
// OutBoundOrdersDTO.java - 用于接收创建请求
@Data
public class OutboundOrderDTO {
    private Long id;
    private String salesOrderCode;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date outboundDate;
    private String remark;

    private List<OutboundItemDTO> items;

    @Data
    public static class OutboundItemDTO {
        private String productCode;      // 改为传入业务编号
        private List<WarehouseStockVO> warehouseDetails;
        private String serialNumbers;
        private String remark;
    }
}

