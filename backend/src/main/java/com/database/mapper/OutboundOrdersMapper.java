package com.database.mapper;

import com.database.pojo.OutboundOrders;

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

}
