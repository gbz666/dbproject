package com.database.service;

import com.database.mapper.CommonMapper;
import com.database.mapper.WarehousesMapper;
import com.database.pojo.Warehouses;
import com.database.vo.BaseSelectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommonService {
    @Autowired
    private CommonMapper commonMapper;
    @Autowired
    private WarehousesMapper warehousesMapper;

    public PageInfo<BaseSelectVO> getCustomerPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectCustomerOptions(keyword);
        return new PageInfo<>(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public PageInfo<BaseSelectVO> getProductPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectProductOptions(keyword);
        return new PageInfo<>(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public PageInfo<BaseSelectVO> getSupplierPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectSupplierOptions(keyword);
        return new PageInfo<>(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public PageInfo<BaseSelectVO> getProductType(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectProductType(keyword);
        return new PageInfo<>(list);
    }

    /** 查询未删除的仓库列表（出库/入库下拉用） */
    public List<Warehouses> getWarehouseList() {
        List<Warehouses> list = warehousesMapper.selectListNotDeleted();
        return list != null ? list : Collections.emptyList();
    }
}