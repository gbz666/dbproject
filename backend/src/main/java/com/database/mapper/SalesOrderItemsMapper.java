package com.database.mapper;

import com.database.pojo.SalesOrderItems;

/**
* @author 高柏舟
* @description 针对表【sales_order_items(销售订单行项目表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.SalesOrderItems
*/
public interface SalesOrderItemsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SalesOrderItems record);

    int insertSelective(SalesOrderItems record);

    SalesOrderItems selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SalesOrderItems record);

    int updateByPrimaryKey(SalesOrderItems record);

}
