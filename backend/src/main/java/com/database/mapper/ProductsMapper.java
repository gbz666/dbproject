package com.database.mapper;

import com.database.pojo.Products;

/**
* @author 高柏舟
* @description 针对表【products(产品表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Products
*/
public interface ProductsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Products record);

    int insertSelective(Products record);

    Products selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Products record);

    int updateByPrimaryKey(Products record);

}
