package com.database.mapper;

import com.database.dto.PaymentExpenseQuery;
import com.database.pojo.PaymentExpenses;
import com.database.vo.PaymentExpenseVO;

import java.util.List;

public interface PaymentExpensesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PaymentExpenses record);

    int insertSelective(PaymentExpenses record);

    PaymentExpenses selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PaymentExpenses record);

    int updateByPrimaryKey(PaymentExpenses record);

    List<PaymentExpenseVO> selectPageList(PaymentExpenseQuery query);
}
