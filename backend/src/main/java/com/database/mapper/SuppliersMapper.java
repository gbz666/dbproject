package com.database.mapper;

import com.database.pojo.Suppliers;

/**
* @author 高柏舟
* @description 针对表【suppliers(供应商基础信息表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Suppliers
*/
public interface SuppliersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Suppliers record);

    int insertSelective(Suppliers record);

    Suppliers selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Suppliers record);

    int updateByPrimaryKey(Suppliers record);

}
