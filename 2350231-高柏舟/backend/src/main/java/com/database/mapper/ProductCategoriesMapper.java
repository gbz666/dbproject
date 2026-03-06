package com.database.mapper;

import com.database.pojo.ProductCategories;
import org.apache.ibatis.annotations.Param;

/**
* @author 高柏舟
* @description 针对表【product_categories(商品分类表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.ProductCategories
*/
public interface ProductCategoriesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ProductCategories record);

    int insertSelective(ProductCategories record);

    ProductCategories selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductCategories record);

    int updateByPrimaryKey(ProductCategories record);
    ProductCategories selectByName(@Param("categoryName") String categoryName);
}
