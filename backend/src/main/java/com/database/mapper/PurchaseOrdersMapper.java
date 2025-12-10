package com.database.mapper;

import com.database.pojo.PurchaseOrders;

/**
* @author 高柏舟
* @description 针对表【purchase_orders(采购订单表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.PurchaseOrders
*/
public interface PurchaseOrdersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PurchaseOrders record);

    int insertSelective(PurchaseOrders record);

    PurchaseOrders selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PurchaseOrders record);

    int updateByPrimaryKey(PurchaseOrders record);

}
