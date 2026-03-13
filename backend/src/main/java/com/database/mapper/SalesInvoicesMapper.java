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
     * @deprecated 已改用先分页发票ID再聚合明细
     */
    List<SalesInvoiceVO> selectInvoicesPage(@Param("query") SalesInvoiceQuery query);

    /**
     * 分页查询：只查符合条件的销项发票 ID 列表（一张发票一条）
     */
    List<Long> selectInvoiceIdsPage(@Param("query") SalesInvoiceQuery query);

    /**
     * 根据发票 ID 列表查询并聚合明细，返回每张发票一条 VO
     * @param ids 发票 ID 列表，返回顺序与传入顺序一致
     */
    List<SalesInvoiceVO> selectVOByIdsWithAggregatedDetails(@Param("ids") List<Long> ids);

    /**
     * 根据主键查询完整 VO
     */
    SalesInvoiceVO selectVOById(Long id);

    /**
     * 软删除
     */
    void softDelete(Long id);

}

