package com.database.mapper;

import com.database.dto.SalesInvoiceQuery;
import com.database.pojo.SalesInvoices;
import com.database.vo.SalesInvoiceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 销项发票表 Mapper
*/
public interface SalesInvoicesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SalesInvoices record);

    int insertSelective(SalesInvoices record);

    SalesInvoices selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SalesInvoices record);

    int updateByPrimaryKey(SalesInvoices record);

    /**
     * 分页查询销项发票 VO 列表（含统计字段）
     */
    List<SalesInvoiceVO> selectInvoicesPage(@Param("query") SalesInvoiceQuery query);

    /**
     * 根据主键查询完整 VO
     */
    SalesInvoiceVO selectVOById(Long id);

    /**
     * 软删除
     */
    void softDelete(Long id);

}

