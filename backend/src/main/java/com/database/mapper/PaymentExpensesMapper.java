package com.database.mapper;

import com.database.pojo.PaymentExpenses;

/**
* @author 高柏舟
* @description 针对表【payment_expenses(付款记录表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.PaymentExpenses
*/
public interface PaymentExpensesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PaymentExpenses record);

    int insertSelective(PaymentExpenses record);

    PaymentExpenses selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PaymentExpenses record);

    int updateByPrimaryKey(PaymentExpenses record);

}
