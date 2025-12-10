package com.database.service;

import com.database.mapper.CustomersMapper;
import com.database.pojo.Customers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CustomerService {
    CustomersMapper customersMapper;
    @Autowired
    public CustomerService(CustomersMapper customersMapper) {
        this.customersMapper = customersMapper;
    }

    public PageInfo<Customers> getCustomersByPage(int pageNum, int pageSize) {
        // 核心步骤 1: 启动分页，接下来的第一次 MyBatis 查询会被拦截并分页
        PageHelper.startPage(pageNum, pageSize);

        // 核心步骤 2: 执行您正常的查询方法
        List<Customers> customerList = customersMapper.selectAllCustomers();

        // 核心步骤 3: 使用 PageInfo 封装结果，它包含了总页数、总条数等信息
        return new PageInfo<>(customerList);
    }
}
