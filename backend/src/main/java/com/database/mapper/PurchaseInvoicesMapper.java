package com.database.mapper;

import com.database.dto.PurchaseInvoiceQuery;
import com.database.pojo.PurchaseInvoices;
import com.database.vo.PurchaseInvoiceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【purchase_invoices(进项发票表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.PurchaseInvoices
*/
public interface PurchaseInvoicesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PurchaseInvoices record);

    int insertSelective(PurchaseInvoices record);

    PurchaseInvoices selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PurchaseInvoices record);

    int updateByPrimaryKey(PurchaseInvoices record);

    /**
     * 分页查询：只查符合条件的进项发票 ID 列表（一张发票一条）
     */
    List<Long> selectInvoiceIdsPage(@Param("query") PurchaseInvoiceQuery query);

    /**
     * 根据发票 ID 列表批量查询并聚合明细，返回每张发票一条 VO
     */
    List<PurchaseInvoiceVO> selectVOByIdsWithAggregatedDetails(@Param("ids") List<Long> ids);

    PurchaseInvoiceVO selectVOById(Long id);

    void softDelete(Long id);
}
