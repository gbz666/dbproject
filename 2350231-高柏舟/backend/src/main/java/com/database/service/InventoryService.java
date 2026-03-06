package com.database.service;

import com.database.mapper.InventoryMapper;
import com.database.vo.InventoryVO;
import com.github.pagehelper.Page;
import org.springframework.stereotype.Service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    public PageInfo<InventoryVO> getInventoryByPage(int pageNum, int pageSize, String productCode,String productName) {
        // 1. 开启分页 (PageHelper 核心原理：通过 ThreadLocal 传递分页参数给 SQL)
        PageHelper.startPage(pageNum, pageSize);

        // 2. 调用 MyBatis 聚合查询方法
        List<InventoryVO> list = inventoryMapper.selectInventoryDetails(productCode,productName);

        // 3. 封装为 PageInfo 返回（包含 total, pages, list 等所有分页信息）
        return new PageInfo<>(list);
    }
}