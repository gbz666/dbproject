package com.database.mapper;

import com.database.pojo.Customers;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【customers(客户基础信息表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Customers
*/
public interface CustomersMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Customers record);

    int insertSelective(Customers record);

    Customers selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Customers record);

    int updateByPrimaryKey(Customers record);
    List<Customers> selectAllCustomers();
}
