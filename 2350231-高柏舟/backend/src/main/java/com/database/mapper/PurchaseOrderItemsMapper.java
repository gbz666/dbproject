package com.database.mapper;

import com.database.pojo.PurchaseOrderItems;

/**
* @author 高柏舟
* @description 针对表【purchase_order_items(采购订单行项目表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.PurchaseOrderItems
*/
public interface PurchaseOrderItemsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PurchaseOrderItems record);

    int insertSelective(PurchaseOrderItems record);

    PurchaseOrderItems selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PurchaseOrderItems record);

    int updateByPrimaryKey(PurchaseOrderItems record);

    void deleteByPurchaseOrderId(Long id);
}
