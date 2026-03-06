package com.database.mapper;

import com.database.pojo.StockIns;
import com.database.vo.StockInVO;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【stock_ins(入库单/入库记录表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.StockIns
*/
public interface StockInsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockIns record);

    int insertSelective(StockIns record);

    StockIns selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockIns record);

    int updateByPrimaryKey(StockIns record);

    void softDeleteStockIn(Long id, Long operatorId);

    List<StockInVO> selectStockInDetails(String purchaseOrderCode, String supplierName, String productName,String serialNumber);

    StockIns selectByPurchaseOrderId(Long purchaseOrderId);
}
