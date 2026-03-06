package com.database.mapper;

import com.database.pojo.PaymentReceipts;

/**
* @author 高柏舟
* @description 针对表【payment_receipts(客户收款记录表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.PaymentReceipts
*/
public interface PaymentReceiptsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PaymentReceipts record);

    int insertSelective(PaymentReceipts record);

    PaymentReceipts selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PaymentReceipts record);

    int updateByPrimaryKey(PaymentReceipts record);

}
