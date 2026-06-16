package com.database.service;

import com.database.dto.PaymentExpenseDTO;
import com.database.dto.PaymentExpenseQuery;
import com.database.mapper.PaymentExpensesMapper;
import com.database.pojo.PaymentExpenses;
import com.database.vo.PaymentExpenseVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentExpenseService {

    @Autowired
    private PaymentExpensesMapper mapper;

    @Transactional(readOnly = true)
    public PageInfo<PaymentExpenseVO> findPage(PaymentExpenseQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<PaymentExpenseVO> list = mapper.selectPageList(query);
        return new PageInfo<>(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(PaymentExpenseDTO dto, Long staffId) {
        PaymentExpenses record = new PaymentExpenses();
        record.setPaymentNo(dto.getPaymentNo());
        record.setSupplierId(dto.getSupplierId());
        record.setPurchaseInvoiceId(dto.getPurchaseInvoiceId());
        record.setAmount(dto.getAmount());
        record.setPaymentDate(dto.getPaymentDate() != null ? java.sql.Date.valueOf(dto.getPaymentDate()) : null);
        record.setMethod(dto.getMethod());
        record.setRemark(dto.getRemark());
        record.setCreatedById(staffId);
        record.setUpdatedById(staffId);
        mapper.insertSelective(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PaymentExpenseDTO dto, Long staffId) {
        PaymentExpenses record = new PaymentExpenses();
        record.setId(id);
        record.setPaymentNo(dto.getPaymentNo());
        record.setSupplierId(dto.getSupplierId());
        record.setPurchaseInvoiceId(dto.getPurchaseInvoiceId());
        record.setAmount(dto.getAmount());
        record.setPaymentDate(dto.getPaymentDate() != null ? java.sql.Date.valueOf(dto.getPaymentDate()) : null);
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
