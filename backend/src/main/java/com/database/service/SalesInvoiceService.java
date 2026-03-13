package com.database.service;

import com.database.dto.SalesInvoiceDTO;
import com.database.dto.SalesInvoiceDetailItemDTO;
import com.database.dto.SalesInvoiceQuery;
import com.database.mapper.SalesInvoiceDetailsMapper;
import com.database.mapper.SalesInvoicesMapper;
import com.database.mapper.SalesOrdersMapper;
import com.database.pojo.SalesInvoiceDetails;
import com.database.pojo.SalesInvoices;
import com.database.pojo.SalesOrders;
import com.database.vo.SalesInvoiceVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SalesInvoiceService {

    @Autowired
    private SalesInvoicesMapper invoiceMapper;

    @Autowired
    private SalesInvoiceDetailsMapper detailMapper;

    @Autowired
    private SalesOrdersMapper salesOrdersMapper;

    /**
     * 分页查询销项发票列表
     * 实现：先按发票分页（一张发票一项），再聚合该页发票的明细
     */
    @Transactional(readOnly = true)
    public PageInfo<SalesInvoiceVO> findPage(SalesInvoiceQuery query) {
        // 1. 先按发票分页：只查符合条件的发票 ID
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<Long> ids = invoiceMapper.selectInvoiceIdsPage(query);
        PageInfo<Long> idPage = new PageInfo<>(ids);

        // 2. 若本页无发票，直接返回空
        if (ids == null || ids.isEmpty()) {
            PageInfo<SalesInvoiceVO> result = new PageInfo<>();
            result.setList(new ArrayList<>());
            result.setTotal(idPage.getTotal());
            result.setPages(idPage.getPages());
            result.setPageNum(idPage.getPageNum());
            result.setPageSize(idPage.getPageSize());
            return result;
        }

        // 3. 根据发票 ID 列表查询并聚合明细（每张发票一条 VO）
        List<SalesInvoiceVO> list = invoiceMapper.selectVOByIdsWithAggregatedDetails(ids);

        PageInfo<SalesInvoiceVO> result = new PageInfo<>();
        result.setList(list);
        result.setTotal(idPage.getTotal());
        result.setPages(idPage.getPages());
        result.setPageNum(idPage.getPageNum());
        result.setPageSize(idPage.getPageSize());
        result.setHasNextPage(idPage.isHasNextPage());
        result.setHasPreviousPage(idPage.isHasPreviousPage());
        return result;
    }

    /**
     * 新增销项发票（主表 + 多条明细）
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesInvoiceVO createInvoice(SalesInvoiceDTO dto, Long staffId) {
        List<SalesInvoiceDetailItemDTO> items = resolveItems(dto);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("销项发票至少需要一行明细");
        }

        // 1. 根据销售订单号反查订单和客户
        Long salesOrderId = null;
        Long customerId = null;
        if (dto.getSalesOrderCode() != null && !dto.getSalesOrderCode().trim().isEmpty()) {
            salesOrderId = salesOrdersMapper.selectOrderIdByCode(dto.getSalesOrderCode().trim());
            if (salesOrderId != null) {
                SalesOrders order = salesOrdersMapper.selectByPrimaryKey(salesOrderId);
                if (order != null) {
                    customerId = order.getCustomerId();
                }
            }
        }

        // 2. 计算含税总金额
        BigDecimal totalAmount = items.stream()
                .map(SalesInvoiceDetailItemDTO::getAmountInclusiveTax)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 写入主表
        SalesInvoices main = new SalesInvoices();
        main.setInvoiceNo(dto.getInvoiceNo());
        if (dto.getInvoiceDate() != null) {
            main.setInvoiceDate(Date.valueOf(dto.getInvoiceDate()));
        }
        main.setSalesOrderId(salesOrderId);
        main.setCustomerId(customerId);
        main.setAmount(scaleToSix(totalAmount));
        main.setRemark(dto.getRemark());
        main.setStatus("unsettled");
        main.setCreatedById(staffId);
        invoiceMapper.insertSelective(main);

        // 4. 写入明细表（逐条插入）
        for (SalesInvoiceDetailItemDTO item : items) {
            SalesInvoiceDetails detail = toDetail(main.getId(), item, staffId, null);
            detailMapper.insertSelective(detail);
        }

        // 返回带完整明细和聚合字段的 VO
        return getById(main.getId());
    }

    /**
     * 更新销项发票（能 UPDATE 的按 id 更新，新增的 INSERT，删除的 DELETE，支持一票多明细）
     * 明细按顺序匹配：前 N 条与原明细一一对应则 UPDATE（WHERE id=?，主键已索引）
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesInvoiceVO updateInvoice(Long id, SalesInvoiceDTO dto, Long staffId) {
        List<SalesInvoiceDetailItemDTO> items = resolveItems(dto);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("销项发票至少需要一行明细");
        }

        // 1. 根据订单号确定外键
        Long salesOrderId = null;
        Long customerId = null;
        if (dto.getSalesOrderCode() != null && !dto.getSalesOrderCode().trim().isEmpty()) {
            salesOrderId = salesOrdersMapper.selectOrderIdByCode(dto.getSalesOrderCode().trim());
            if (salesOrderId != null) {
                SalesOrders order = salesOrdersMapper.selectByPrimaryKey(salesOrderId);
                if (order != null) {
                    customerId = order.getCustomerId();
                }
            }
        }

        // 2. 计算含税总金额
        BigDecimal totalAmount = items.stream()
                .map(SalesInvoiceDetailItemDTO::getAmountInclusiveTax)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 更新主表
        SalesInvoices main = new SalesInvoices();
        main.setId(id);
        main.setInvoiceNo(dto.getInvoiceNo());
        if (dto.getInvoiceDate() != null) {
            main.setInvoiceDate(Date.valueOf(dto.getInvoiceDate()));
        }
        main.setSalesOrderId(salesOrderId);
        main.setCustomerId(customerId);
        main.setAmount(scaleToSix(totalAmount));
        main.setRemark(dto.getRemark());
        main.setUpdatedById(staffId);
        invoiceMapper.updateByPrimaryKeySelective(main);

        // 4. 明细：能按 id 更新的 UPDATE，多出的 INSERT，少掉的 DELETE
        List<SalesInvoiceDetails> existing = detailMapper.selectByInvoiceId(id);
        int n = Math.min(existing.size(), items.size());
        for (int i = 0; i < n; i++) {
            SalesInvoiceDetails detail = toDetail(id, items.get(i), null, staffId);
            detail.setId(existing.get(i).getId());
            detail.setUpdatedAt(new java.util.Date());
            detailMapper.updateByPrimaryKeySelective(detail);
        }
        for (int i = n; i < items.size(); i++) {
            SalesInvoiceDetails detail = toDetail(id, items.get(i), staffId, staffId);
            detailMapper.insertSelective(detail);
        }
        for (int i = n; i < existing.size(); i++) {
            detailMapper.deleteByPrimaryKey(existing.get(i).getId());
        }

        // 返回带完整明细和聚合字段的 VO
        return getById(id);
    }

    /**
     * 软删除销项发票
     */
    @Transactional
    public void deleteInvoice(Long id) {
        invoiceMapper.softDelete(id);
    }

    /**
     * 根据 ID 查询销项发票详情：包含一票多明细 + 聚合后的展示字段
     */
    @Transactional(readOnly = true)
    public SalesInvoiceVO getById(Long id) {
        SalesInvoiceVO vo = invoiceMapper.selectVOById(id);
        if (vo == null) {
            return null;
        }
        fillDetailsAndAggregate(vo);
        return vo;
    }

    /**
     * 为销项发票 VO 填充明细列表，并在 Java 层按“一票多明细”聚合展示字段
     */
    private void fillDetailsAndAggregate(SalesInvoiceVO vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        List<SalesInvoiceDetails> details = detailMapper.selectByInvoiceId(vo.getId());
        if (details == null || details.isEmpty()) {
            vo.setItems(List.of());
            return;
        }

        // 明细列表转换为 items（DTO 列表），供前端编辑使用
        List<SalesInvoiceDetailItemDTO> items = details.stream().map(d -> {
            SalesInvoiceDetailItemDTO item = new SalesInvoiceDetailItemDTO();
            item.setItemName(d.getItemName());
            item.setSpecification(d.getSpecification());
            item.setUnit(d.getUnit());
            item.setQuantity(d.getQuantity());
            item.setUnitPriceInclusiveTax(d.getUnitPrice());
            item.setAmountExclusiveTax(d.getAmountExclusiveTax());
            item.setTaxAmount(d.getTaxAmount());
            item.setAmountInclusiveTax(d.getAmountInclusiveTax());
            item.setRemark(d.getRemark());
            return item;
        }).collect(Collectors.toList());
        vo.setItems(items);

        // 货物名称、规格型号：用中文分号连接；单位去重
        vo.setItemName(details.stream()
                .map(SalesInvoiceDetails::getItemName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("；")));
        vo.setSpecification(details.stream()
                .map(SalesInvoiceDetails::getSpecification)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("；")));
        vo.setUnit(details.stream()
                .map(SalesInvoiceDetails::getUnit)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining("；")));

        // 数量与金额：求和
        vo.setQuantity(details.stream()
                .map(SalesInvoiceDetails::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setAmountExclusiveTax(details.stream()
                .map(SalesInvoiceDetails::getAmountExclusiveTax)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setTaxAmount(details.stream()
                .map(SalesInvoiceDetails::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setAmountInclusiveTax(details.stream()
                .map(SalesInvoiceDetails::getAmountInclusiveTax)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 单价：若只有一行明细则直接取，否则置空（避免多行语义不清）
        if (details.size() == 1) {
            vo.setUnitPriceInclusiveTax(details.get(0).getUnitPrice());
        } else {
            vo.setUnitPriceInclusiveTax(null);
        }
    }

    /**
     * 从 DTO 解析明细列表：优先用 items；若为空则从扁平字段构建单条（兼容旧前端）
     */
    private List<SalesInvoiceDetailItemDTO> resolveItems(SalesInvoiceDTO dto) {
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            return dto.getItems();
        }
        if (dto.getItemName() != null || dto.getQuantity() != null || dto.getAmountInclusiveTax() != null) {
            SalesInvoiceDetailItemDTO one = new SalesInvoiceDetailItemDTO();
            one.setItemName(dto.getItemName());
            one.setSpecification(dto.getSpecification());
            one.setUnit(dto.getUnit());
            one.setQuantity(dto.getQuantity());
            one.setUnitPriceInclusiveTax(dto.getUnitPriceInclusiveTax());
            one.setAmountExclusiveTax(dto.getAmountExclusiveTax());
            one.setTaxAmount(dto.getTaxAmount());
            one.setAmountInclusiveTax(dto.getAmountInclusiveTax());
            one.setRemark(dto.getRemark());
            return List.of(one);
        }
        return List.of();
    }

    private SalesInvoiceDetails toDetail(Long invoiceId, SalesInvoiceDetailItemDTO item, Long createdById, Long updatedById) {
        SalesInvoiceDetails detail = new SalesInvoiceDetails();
        detail.setInvoiceId(invoiceId);
        detail.setItemName(item.getItemName());
        detail.setSpecification(item.getSpecification());
        detail.setUnit(item.getUnit());
        detail.setQuantity(scaleToSix(item.getQuantity()));
        detail.setUnitPrice(scaleToSix(item.getUnitPriceInclusiveTax()));
        detail.setAmountExclusiveTax(scaleToSix(item.getAmountExclusiveTax()));
        detail.setTaxAmount(scaleToSix(item.getTaxAmount()));
        detail.setAmountInclusiveTax(scaleToSix(item.getAmountInclusiveTax()));
        detail.setRemark(item.getRemark());
        detail.setCreatedById(createdById);
        if (updatedById != null) {
            detail.setUpdatedById(updatedById);
        }
        return detail;
    }

    /**
     * 统一小数位（最多 6 位），允许 null
     */
    private BigDecimal scaleToSix(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(6, RoundingMode.HALF_UP);
    }
}

