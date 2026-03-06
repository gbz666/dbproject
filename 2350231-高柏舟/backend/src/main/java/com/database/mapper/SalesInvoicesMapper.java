package com.database.mapper;

import com.database.pojo.SalesInvoices;

/**
* @author 高柏舟
* @description 针对表【sales_invoices(销项发票表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.SalesInvoices
*/
public interface SalesInvoicesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SalesInvoices record);

    int insertSelective(SalesInvoices record);

    SalesInvoices selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SalesInvoices record);

    int updateByPrimaryKey(SalesInvoices record);

}
