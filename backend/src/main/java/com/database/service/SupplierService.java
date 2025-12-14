package com.database.service;

import com.database.mapper.SuppliersMapper;
import com.database.pojo.Customers;
import com.database.pojo.Suppliers;
import com.database.vo.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@Slf4j
public class SupplierService {
    private SuppliersMapper suppliersMapper;
    SupplierService(SuppliersMapper suppliersMapper) {
        this.suppliersMapper = suppliersMapper;
    }
    @Transactional
    public PageInfo<Suppliers> getSuppliersByPage(int pageNum, int pageSize) {
        // 核心步骤 1: 启动分页，接下来的第一次 MyBatis 查询会被拦截并分页
        PageHelper.startPage(pageNum, pageSize);

        // 核心步骤 2: 执行您正常的查询方法
        List<Suppliers> supplierList = suppliersMapper.selectAllSuppliers();

        // 核心步骤 3: 使用 PageInfo 封装结果，它包含了总页数、总条数等信息
        return new PageInfo<>(supplierList);
    }
    @Transactional
    public Suppliers createSupplier(Suppliers suppliers) {
        Long id = (long) suppliersMapper.insert(suppliers);
        Suppliers supplier = suppliersMapper.selectByPrimaryKey(id);

        return supplier;
    }
}
