package com.database.mapper;

import com.database.pojo.OutboundOrderItems;

/**
* @author 高柏舟
* @description 针对表【outbound_order_items(出库单行项目表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.OutboundOrderItems
*/
public interface OutboundOrderItemsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(OutboundOrderItems record);

    int insertSelective(OutboundOrderItems record);

    OutboundOrderItems selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OutboundOrderItems record);

    int updateByPrimaryKey(OutboundOrderItems record);

}
