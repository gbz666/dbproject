package com.database.mapper;

import com.database.pojo.OutboundOrders;
import com.database.vo.OutboundDetailVO;
import com.database.vo.WarehouseStockVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
* @author 高柏舟
* @description 针对表【outbound_orders(出库单表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.OutboundOrders
*/
public interface OutboundOrdersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OutboundOrders record);

    int insertSelective(OutboundOrders record);

    OutboundOrders selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OutboundOrders record);

    int updateByPrimaryKey(OutboundOrders record);


    void softDeleteOrder(Long id, Long operatorId);

    List<OutboundDetailVO> selectOutboundDetails(String salesOrderCode, String customerName, String productName, String serialNumber);


}
