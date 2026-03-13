package com.database.mapper;

import com.database.pojo.SalesInvoiceDetails;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【sales_invoice_details(销项发票明细)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.SalesInvoiceDetails
*/
public interface SalesInvoiceDetailsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SalesInvoiceDetails record);

    int insertSelective(SalesInvoiceDetails record);

    SalesInvoiceDetails selectByPrimaryKey(Long id);

    List<SalesInvoiceDetails> selectByInvoiceId(Long invoiceId);

    int deleteByInvoiceId(Long invoiceId);

    int updateByPrimaryKeySelective(SalesInvoiceDetails record);

    int updateByPrimaryKey(SalesInvoiceDetails record);

    /**
     * 根据发票ID更新对应明细
     */
    void updateByInvoiceId(SalesInvoiceDetails detail);

}
