package com.database.mapper;

import com.database.vo.BaseSelectVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommonMapper {
    // 对应 XML 中的 id
    List<BaseSelectVO> selectCustomerOptions(@Param("keyword") String keyword);

    List<BaseSelectVO> selectProductOptions(@Param("keyword") String keyword);

    List<BaseSelectVO> selectSupplierOptions(@Param("keyword") String keyword);

    List<BaseSelectVO> selectProductType(String keyword);
}