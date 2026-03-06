package com.database.mapper;

import com.database.pojo.Inventory;
import com.database.vo.InventoryVO;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 原子扣减库存：仅当当前库存 >= 扣减数量时更新，防止超卖。
     * @return 影响行数，1 表示扣减成功，0 表示库存不足
     */
    int deductInventoryIfSufficient(Long productId, Long warehouseId, BigDecimal quantity);

    List<InventoryVO> selectInventoryDetails(String productCode,String productName);
}
