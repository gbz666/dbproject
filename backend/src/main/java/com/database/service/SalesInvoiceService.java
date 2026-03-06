package com.database.service;

import com.database.dto.SalesInvoiceDTO;
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
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;

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
     */
    @Transactional(readOnly = true)
    public PageInfo<SalesInvoiceVO> findPage(SalesInvoiceQuery query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return new PageInfo<>(invoiceMapper.selectInvoicesPage(query));
    }

    /**
     * 新增销项发票（主表 + 明细各 1 条）
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesInvoiceVO createInvoice(SalesInvoiceDTO dto, Long staffId) {
        // 1. 根据销售订单号反查订单和客户，填充主表外键
        Long salesOrderId = null;
        Long customerId = null;
        if (dto.getSalesOrderCode() != null && !dto.getSalesOrderCode().isEmpty()) {
            salesOrderId = salesOrdersMapper.selectOrderIdByCode(dto.getSalesOrderCode());
            if (salesOrderId != null) {
                SalesOrders order = salesOrdersMapper.selectByPrimaryKey(salesOrderId);
                if (order != null) {
                    customerId = order.getCustomerId();
                }
            }
        }

        // 2. 写入主表 sales_invoices
        SalesInvoices main = new SalesInvoices();
        main.setInvoiceNo(dto.getInvoiceNo());
        if (dto.getInvoiceDate() != null) {
            main.setInvoiceDate(Date.valueOf(dto.getInvoiceDate()));
        }
        main.setSalesOrderId(salesOrderId);
        main.setCustomerId(customerId);
        main.setAmount(scaleToSix(dto.getAmountInclusiveTax()));
        main.setRemark(dto.getRemark());
        main.setStatus("unsettled");
        main.setCreatedById(staffId);
        invoiceMapper.insertSelective(main);

        // 3. 写入明细表 sales_invoice_details
        SalesInvoiceDetails detail = new SalesInvoiceDetails();
        detail.setInvoiceId(main.getId());
        detail.setItemName(dto.getItemName());
        detail.setSpecification(dto.getSpecification());
        detail.setUnit(dto.getUnit());
        detail.setQuantity(scaleToSix(dto.getQuantity()));
        detail.setUnitPrice(scaleToSix(dto.getUnitPriceInclusiveTax()));
        detail.setAmountInclusiveTax(scaleToSix(dto.getAmountInclusiveTax()));
        detail.setAmountExclusiveTax(scaleToSix(dto.getAmountExclusiveTax()));
        detail.setTaxAmount(scaleToSix(dto.getTaxAmount()));
        detail.setRemark(dto.getRemark());
        detail.setCreatedById(staffId);
        detailMapper.insertSelective(detail);

        // 4. 返回带统计字段的 VO
        return invoiceMapper.selectVOById(main.getId());
    }

    /**
     * 更新销项发票
     */
    @Transactional(rollbackFor = Exception.class)
    public SalesInvoiceVO updateInvoice(Long id, SalesInvoiceDTO dto, Long staffId) {
        // 1. 重新根据订单号确定外键关系
        Long salesOrderId = null;
        Long customerId = null;
        if (dto.getSalesOrderCode() != null && !dto.getSalesOrderCode().isEmpty()) {
            salesOrderId = salesOrdersMapper.selectOrderIdByCode(dto.getSalesOrderCode());
            if (salesOrderId != null) {
                SalesOrders order = salesOrdersMapper.selectByPrimaryKey(salesOrderId);
                if (order != null) {
                    customerId = order.getCustomerId();
                }
            }
        }

        // 2. 更新主表
        SalesInvoices main = new SalesInvoices();
        main.setId(id);
        main.setInvoiceNo(dto.getInvoiceNo());
        if (dto.getInvoiceDate() != null) {
            main.setInvoiceDate(Date.valueOf(dto.getInvoiceDate()));
        }
        main.setSalesOrderId(salesOrderId);
        main.setCustomerId(customerId);
        main.setAmount(scaleToSix(dto.getAmountInclusiveTax()));
        main.setRemark(dto.getRemark());
        main.setUpdatedById(staffId);
        invoiceMapper.updateByPrimaryKeySelective(main);

        // 3. 更新明细（按 invoice_id）
        SalesInvoiceDetails detail = new SalesInvoiceDetails();
        detail.setInvoiceId(id);
        detail.setItemName(dto.getItemName());
        detail.setSpecification(dto.getSpecification());
        detail.setUnit(dto.getUnit());
        detail.setQuantity(scaleToSix(dto.getQuantity()));
        detail.setUnitPrice(scaleToSix(dto.getUnitPriceInclusiveTax()));
        detail.setAmountInclusiveTax(scaleToSix(dto.getAmountInclusiveTax()));
        detail.setAmountExclusiveTax(scaleToSix(dto.getAmountExclusiveTax()));
        detail.setTaxAmount(scaleToSix(dto.getTaxAmount()));
        detail.setRemark(dto.getRemark());
        detail.setUpdatedById(staffId);
        detailMapper.updateByInvoiceId(detail);

        return invoiceMapper.selectVOById(id);
    }

    /**
     * 软删除销项发票
     */
    @Transactional
    public void deleteInvoice(Long id) {
        invoiceMapper.softDelete(id);
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

