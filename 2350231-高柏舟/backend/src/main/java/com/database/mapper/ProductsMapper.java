package com.database.mapper;

import com.database.pojo.Products;
import com.database.vo.ProductVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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

    Integer getMaxProductCodeNumber();
    List<ProductVO> selectVOList(
            @Param("productName") String productName,
            @Param("categoryName") String categoryName,
            @Param("productCode" ) String productCode,
            @Param("productType")String productType
    );
    /**
     * 【扩展】逻辑删除产品 (更新 is_deleted=1 和 deleted_at)
     * @param id 产品ID
     * @return 影响的行数
     */
    int softDeleteByPrimaryKey(Long id);

    /** 【扩展】用于详情查询，联表获取 categoryName，返回 VO */
    ProductVO selectVOByPrimaryKey(Long id);

    // 【新增】根据业务编码查找 POJO (Service层内部使用)
    Products selectByProductCode(String productCode);

    // 【新增】根据业务编码查找 VO (Controller/Service对外使用)
    ProductVO selectVOByProductCode(String productCode);

    // 【新增】根据业务编码进行逻辑删除
    int softDeleteByProductCode(String productCode);

    int updateProductCostPrice(Long productId, BigDecimal newCostPrice);
}
