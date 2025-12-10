package com.database.mapper;

import com.database.pojo.StockIns;

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

}
