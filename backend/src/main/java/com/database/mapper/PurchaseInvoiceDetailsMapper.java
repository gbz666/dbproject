package com.database.mapper;

import com.database.pojo.PurchaseInvoiceDetails;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【purchase_invoice_details(进项发票明细)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.PurchaseInvoiceDetails
*/
public interface PurchaseInvoiceDetailsMapper {

    List<PurchaseInvoiceDetails> selectByInvoiceId(Long invoiceId);

    int deleteByInvoiceId(Long invoiceId);

    int deleteByPrimaryKey(Long id);

    int insert(PurchaseInvoiceDetails record);

    int insertSelective(PurchaseInvoiceDetails record);

    PurchaseInvoiceDetails selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PurchaseInvoiceDetails record);

    int updateByPrimaryKey(PurchaseInvoiceDetails record);

    void updateByInvoiceId(PurchaseInvoiceDetails detail);
}
