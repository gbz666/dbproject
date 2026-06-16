package com.database.service;

import com.database.dto.PaymentReceiptDTO;
import com.database.dto.PaymentReceiptQuery;
import com.database.mapper.PaymentReceiptsMapper;
import com.database.pojo.PaymentReceipts;
import com.database.vo.PaymentReceiptVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentReceiptService {

    @Autowired
    private PaymentReceiptsMapper mapper;

    @Transactional(readOnly = true)
    public PageInfo<PaymentReceiptVO> findPage(PaymentReceiptQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<PaymentReceiptVO> list = mapper.selectPageList(query);
        return new PageInfo<>(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PaymentReceiptDTO dto, Long staffId) {
        PaymentReceipts record = new PaymentReceipts();
        record.setReceiptNo(dto.getReceiptNo());
        record.setCustomerId(dto.getCustomerId());
        record.setSalesInvoiceId(dto.getSalesInvoiceId());
        record.setAmount(dto.getAmount());
        record.setReceiptDate(dto.getReceiptDate() != null ? java.sql.Date.valueOf(dto.getReceiptDate()) : null);
        record.setMethod(dto.getMethod());
        record.setRemark(dto.getRemark());
        record.setCreatedById(staffId);
        record.setUpdatedById(staffId);
        mapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PaymentReceiptDTO dto, Long staffId) {
        PaymentReceipts record = new PaymentReceipts();
        record.setId(id);
        record.setReceiptNo(dto.getReceiptNo());
        record.setCustomerId(dto.getCustomerId());
        record.setSalesInvoiceId(dto.getSalesInvoiceId());
        record.setAmount(dto.getAmount());
        record.setReceiptDate(dto.getReceiptDate() != null ? java.sql.Date.valueOf(dto.getReceiptDate()) : null);
        record.setMethod(dto.getMethod());
        record.setRemark(dto.getRemark());
        record.setUpdatedById(staffId);
        mapper.updateByPrimaryKeySelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        mapper.softDelete(id);
    }
}
