package com.database.service;

import com.database.dto.PurchaseInvoiceDTO;
import com.database.dto.PurchaseInvoiceQuery;
import com.database.mapper.PurchaseInvoiceDetailsMapper;
import com.database.mapper.PurchaseInvoicesMapper;
import com.database.pojo.PurchaseInvoiceDetails;
import com.database.pojo.PurchaseInvoices;
import com.database.vo.PurchaseInvoiceVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseInvoiceService {
    @Autowired
    private PurchaseInvoicesMapper invoiceMapper; // 主表 Mapper

    @Autowired
    private PurchaseInvoiceDetailsMapper detailMapper; // 从表 Mapper

    // 分页查询：调用主表 Mapper 关联查询 VO
    @Transactional
    public PageInfo<PurchaseInvoiceVO> findPage(PurchaseInvoiceQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return new PageInfo<>(invoiceMapper.selectInvoicesPage(query));
    }

    // 【新增】
    @Transactional(rollbackFor = Exception.class)
    public PurchaseInvoiceVO createInvoice(PurchaseInvoiceDTO dto, Long staffId) {
        // 1. 插入主表
        PurchaseInvoices main = new PurchaseInvoices();
        BeanUtils.copyProperties(dto, main);
        main.setAmount(dto.getAmountInclusiveTax()); // 设置含税总额
        main.setCreatedById(staffId);
        invoiceMapper.insert(main); // 执行后 main.id 会被自动填充

        // 2. 插入从表 (详情)
        PurchaseInvoiceDetails detail = new PurchaseInvoiceDetails();
        BeanUtils.copyProperties(dto, detail);
        detail.setInvoiceId(main.getId()); // 使用主表生成的 ID
        detail.setCreatedById(staffId);
        detailMapper.insertSelective(detail);

        return invoiceMapper.selectVOById(main.getId());
    }

    // 【修改】
    @Transactional(rollbackFor = Exception.class)
    public PurchaseInvoiceVO updateInvoice(Long id, PurchaseInvoiceDTO dto, Long staffId) {
        // 1. 更新主表
        PurchaseInvoices main = new PurchaseInvoices();
        main.setId(id);
        BeanUtils.copyProperties(dto, main);
        main.setAmount(dto.getAmountInclusiveTax());
        main.setUpdatedById(staffId);
        invoiceMapper.updateByPrimaryKeySelective(main);

        // 2. 更新从表 (根据 invoice_id 更新对应详情)
        PurchaseInvoiceDetails detail = new PurchaseInvoiceDetails();
        BeanUtils.copyProperties(dto, detail);
        detail.setInvoiceId(id);
        detail.setUpdatedById(staffId);
        detailMapper.updateByInvoiceId(detail);

        return invoiceMapper.selectVOById(id);
    }
    @Transactional
    public void deleteInvoice(Long id) {
        invoiceMapper.softDelete(id);
    }
}