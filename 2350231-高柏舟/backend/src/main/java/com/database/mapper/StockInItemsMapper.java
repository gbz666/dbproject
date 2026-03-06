package com.database.mapper;

import com.database.pojo.StockInItems;

import java.math.BigDecimal;
import java.util.List;

/**
* @author 高柏舟
* @description 针对表【stock_in_items(入库行项目表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.StockInItems
*/
public interface StockInItemsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockInItems record);

    int insertSelective(StockInItems record);

    StockInItems selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockInItems record);

    int updateByPrimaryKey(StockInItems record);

    void insertItemDetail(Long stockInId, Long productId, Long warehouseId, BigDecimal quantity, String serialNumbers, Long creatorId);

    List<StockInItems> selectByStockInId(Long stockInId);

    void deleteByStockInId(Long id);
}
