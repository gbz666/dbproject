package com.database.mapper;

import com.database.pojo.Products;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Excel 导入专用 Mapper，提供批量 upsert 等方法
 */
@Mapper
public interface ExcelImportMapper {

    /**
     * 产品 upsert：存在则更新，不存在则插入（按 product_code 唯一）
     */
    int upsertProduct(Products product);

    /**
     * 根据 category_name 获取 category_id
     */
    Integer selectCategoryIdByName(@Param("categoryName") String categoryName);

    Long selectCustomerIdByName(@Param("customerName") String customerName);

    Long selectProductIdByName(@Param("productName") String productName);

    /** 按仓库名称或包含关系查 warehouse_id（未找到返回 null） */
    Long selectWarehouseIdByName(@Param("name") String name);
}
