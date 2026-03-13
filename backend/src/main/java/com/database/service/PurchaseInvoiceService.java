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
import java.util.Objects;
import java.util.stream.Collectors;

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
        // 1. 先分页查发票 ID
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Long> ids = invoiceMapper.selectInvoiceIdsPage(query);
        PageInfo<Long> idPage = new PageInfo<>(ids);

        if (ids == null || ids.isEmpty()) {
            PageInfo<PurchaseInvoiceVO> result = new PageInfo<>();
            result.setList(List.of());
            result.setTotal(idPage.getTotal());
            result.setPages(idPage.getPages());
            result.setPageNum(idPage.getPageNum());
            result.setPageSize(idPage.getPageSize());
            return result;
        }

        // 2. 批量查并聚合明细
        List<PurchaseInvoiceVO> list = invoiceMapper.selectVOByIdsWithAggregatedDetails(ids);
        PageInfo<PurchaseInvoiceVO> result = new PageInfo<>();
        result.setList(list);
        result.setTotal(idPage.getTotal());
        result.setPages(idPage.getPages());
        result.setPageNum(idPage.getPageNum());
        result.setPageSize(idPage.getPageSize());
        result.setHasNextPage(idPage.isHasNextPage());
        result.setHasPreviousPage(idPage.isHasPreviousPage());
        return result;
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceVO getById(Long id) {
        return buildVOWithDetails(id);
    }

    private PurchaseInvoiceVO buildVOWithDetails(Long invoiceId) {
        PurchaseInvoiceVO vo = invoiceMapper.selectVOById(invoiceId);
        if (vo == null) return null;
        fillDetailsAndAggregate(vo);
        return vo;
    }

    /**
     * 为进项发票 VO 填充完整明细列表，并在 Java 层按“一票多明细”做聚合展示字段
     */
    private void fillDetailsAndAggregate(PurchaseInvoiceVO vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        List<PurchaseInvoiceDetails> details = detailMapper.selectByInvoiceId(vo.getId());
        vo.setDetails(details);
        if (details == null || details.isEmpty()) {
            return;
        }

        // 货物名称、规格型号、单位：用中文分号连接，单位去重
        vo.setItemName(details.stream()
                .map(PurchaseInvoiceDetails::getItemName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("；")));
        vo.setSpecification(details.stream()
                .map(PurchaseInvoiceDetails::getSpecification)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("；")));
        vo.setUnit(details.stream()
                .map(PurchaseInvoiceDetails::getUnit)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("；")));

        // 数量与金额：求和
        vo.setQuantity(details.stream()
                .map(PurchaseInvoiceDetails::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setAmountExclusiveTax(details.stream()
                .map(PurchaseInvoiceDetails::getAmountExclusiveTax)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setTaxAmount(details.stream()
                .map(PurchaseInvoiceDetails::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setAmountInclusiveTax(details.stream()
                .map(PurchaseInvoiceDetails::getAmountInclusiveTax)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 单价、税率：若只有一行明细则直接取，否则置空（避免多行语义不清）
        if (details.size() == 1) {
            PurchaseInvoiceDetails first = details.get(0);
            vo.setUnitPrice(first.getUnitPrice());
            vo.setTaxRate(first.getTaxRate());
        } else {
            vo.setUnitPrice(null);
            vo.setTaxRate(null);
        }
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
