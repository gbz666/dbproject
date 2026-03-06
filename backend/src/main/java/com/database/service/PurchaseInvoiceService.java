package com.database.service;

import com.database.dto.PurchaseInvoiceDTO;
import com.database.dto.PurchaseInvoiceDetailItemDTO;
import com.database.dto.PurchaseInvoiceQuery;
import com.database.mapper.PurchaseInvoiceDetailsMapper;
import com.database.mapper.PurchaseInvoicesMapper;
import com.database.mapper.PurchaseOrdersMapper;
import com.database.pojo.PurchaseInvoiceDetails;
import com.database.pojo.PurchaseInvoices;
import com.database.pojo.PurchaseOrders;
import com.database.vo.PurchaseInvoiceVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseInvoiceService {
    @Autowired
    private PurchaseInvoicesMapper invoiceMapper;

    @Autowired
    private PurchaseInvoiceDetailsMapper detailMapper;

    @Autowired
    private PurchaseOrdersMapper purchaseOrdersMapper;

    @Transactional(readOnly = true)
    public PageInfo<PurchaseInvoiceVO> findPage(PurchaseInvoiceQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return new PageInfo<>(invoiceMapper.selectInvoicesPage(query));
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceVO getById(Long id) {
        return buildVOWithDetails(id);
    }

    private PurchaseInvoiceVO buildVOWithDetails(Long invoiceId) {
        PurchaseInvoiceVO vo = invoiceMapper.selectVOById(invoiceId);
        if (vo == null) return null;
        List<PurchaseInvoiceDetails> details = detailMapper.selectByInvoiceId(invoiceId);
        vo.setDetails(details);
        if (details != null && !details.isEmpty()) {
            PurchaseInvoiceDetails first = details.get(0);
            vo.setItemName(first.getItemName());
            vo.setSpecification(first.getSpecification());
            vo.setUnit(first.getUnit());
            vo.setQuantity(first.getQuantity());
            vo.setUnitPrice(first.getUnitPrice());
            vo.setAmountExclusiveTax(first.getAmountExclusiveTax());
            vo.setTaxRate(first.getTaxRate());
            vo.setTaxAmount(first.getTaxAmount());
            vo.setAmountInclusiveTax(first.getAmountInclusiveTax());
            vo.setRemark(first.getRemark());
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseInvoiceVO createInvoice(PurchaseInvoiceDTO dto, Long staffId) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("进项发票至少需要一行明细");
        }
        BigDecimal totalAmount = dto.getItems().stream()
                .map(PurchaseInvoiceDetailItemDTO::getAmountInclusiveTax)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseInvoices main = new PurchaseInvoices();
        main.setInvoiceNo(dto.getInvoiceNo());
        main.setInvoiceDate(dto.getInvoiceDate() != null ? java.sql.Date.valueOf(dto.getInvoiceDate()) : null);
        main.setSupplierId(dto.getSupplierId());
        main.setRemark(dto.getRemark());
        main.setAmount(totalAmount);
        main.setCreatedById(staffId);
        main.setIsDeleted(0);
        if (dto.getPurchaseCode() != null && !dto.getPurchaseCode().trim().isEmpty()) {
            Long orderId = purchaseOrdersMapper.selectOrderIdByCode(dto.getPurchaseCode().trim());
            main.setPurchaseOrderId(orderId);
            if (dto.getSupplierId() == null && orderId != null) {
                PurchaseOrders order = purchaseOrdersMapper.selectByPrimaryKey(orderId);
                if (order != null) main.setSupplierId(order.getSupplierId());
            }
        }
        invoiceMapper.insert(main);

        for (PurchaseInvoiceDetailItemDTO item : dto.getItems()) {
            PurchaseInvoiceDetails detail = new PurchaseInvoiceDetails();
            BeanUtils.copyProperties(item, detail);
            detail.setInvoiceId(main.getId());
            detail.setCreatedById(staffId);
            detailMapper.insertSelective(detail);
        }
        return buildVOWithDetails(main.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public PurchaseInvoiceVO updateInvoice(Long id, PurchaseInvoiceDTO dto, Long staffId) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("进项发票至少需要一行明细");
        }
        BigDecimal totalAmount = dto.getItems().stream()
                .map(PurchaseInvoiceDetailItemDTO::getAmountInclusiveTax)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PurchaseInvoices main = new PurchaseInvoices();
        main.setId(id);
        main.setInvoiceNo(dto.getInvoiceNo());
        main.setInvoiceDate(dto.getInvoiceDate() != null ? java.sql.Date.valueOf(dto.getInvoiceDate()) : null);
        main.setSupplierId(dto.getSupplierId());
        main.setRemark(dto.getRemark());
        main.setAmount(totalAmount);
        main.setUpdatedById(staffId);
        if (dto.getPurchaseCode() != null && !dto.getPurchaseCode().trim().isEmpty()) {
            Long orderId = purchaseOrdersMapper.selectOrderIdByCode(dto.getPurchaseCode().trim());
            main.setPurchaseOrderId(orderId);
        }
        invoiceMapper.updateByPrimaryKeySelective(main);

        detailMapper.deleteByInvoiceId(id);
        for (PurchaseInvoiceDetailItemDTO item : dto.getItems()) {
            PurchaseInvoiceDetails detail = new PurchaseInvoiceDetails();
            BeanUtils.copyProperties(item, detail);
            detail.setInvoiceId(id);
            detail.setCreatedById(staffId);
            detail.setUpdatedById(staffId);
            detailMapper.insertSelective(detail);
        }
        return buildVOWithDetails(id);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        invoiceMapper.softDelete(id);
    }
}
