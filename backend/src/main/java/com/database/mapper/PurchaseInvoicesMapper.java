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

    // 复杂的 VO 关联查询，包含所有统计字段
    List<PurchaseInvoiceVO> selectInvoicesPage(@Param("query") PurchaseInvoiceQuery query);


    PurchaseInvoiceVO selectVOById(Long id);

    void softDelete(Long id);
}
