package com.database.mapper;

import com.database.pojo.Inventory;

import java.math.BigDecimal;

/**
* @author 高柏舟
* @description 针对表【inventory(库存表(复合主键 product_id+warehouse_id))】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Inventory
*/
public interface InventoryMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Inventory record);

    int insertSelective(Inventory record);

    Inventory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Inventory record);

    int updateByPrimaryKey(Inventory record);

    Long selectProductNum(Long id);

    void updateInventory(Long productId, Long warehouseId, BigDecimal quantity);
}
