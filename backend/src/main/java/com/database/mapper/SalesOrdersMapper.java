package com.database.mapper;

import com.database.pojo.SalesOrders;

/**
* @author 高柏舟
* @description 针对表【sales_orders(销售订单表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.SalesOrders
*/
public interface SalesOrdersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SalesOrders record);

    int insertSelective(SalesOrders record);

    SalesOrders selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SalesOrders record);

    int updateByPrimaryKey(SalesOrders record);

}
