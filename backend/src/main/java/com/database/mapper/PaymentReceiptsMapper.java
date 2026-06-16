package com.database.mapper;

import com.database.dto.PaymentReceiptQuery;
import com.database.pojo.PaymentReceipts;
import com.database.vo.PaymentReceiptVO;

import java.util.List;

public interface PaymentReceiptsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PaymentReceipts record);

    int insertSelective(PaymentReceipts record);

    PaymentReceipts selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PaymentReceipts record);

    int updateByPrimaryKey(PaymentReceipts record);

    List<PaymentReceiptVO> selectPageList(PaymentReceiptQuery query);

    void softDelete(Long id);
}
