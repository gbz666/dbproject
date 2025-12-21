package com.database.service;

import com.database.mapper.CommonMapper;
import com.database.vo.BaseSelectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class CommonService {
    @Autowired
    private CommonMapper commonMapper;

    public PageInfo<BaseSelectVO> getCustomerPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectCustomerOptions(keyword);
        return new PageInfo<>(list);
    }

    public PageInfo<BaseSelectVO> getProductPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectProductOptions(keyword);
        return new PageInfo<>(list);
    }

    public PageInfo<BaseSelectVO> getSupplierPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<BaseSelectVO> list = commonMapper.selectSupplierOptions(keyword);
        return new PageInfo<>(list);
    }
}