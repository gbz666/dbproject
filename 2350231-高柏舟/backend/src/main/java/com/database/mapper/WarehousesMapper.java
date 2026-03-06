package com.database.mapper;

import com.database.pojo.Warehouses;

/**
* @author 高柏舟
* @description 针对表【warehouses(仓库表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Warehouses
*/
public interface WarehousesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Warehouses record);

    int insertSelective(Warehouses record);

    Warehouses selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Warehouses record);

    int updateByPrimaryKey(Warehouses record);

}
