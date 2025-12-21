package com.database.service;

import com.database.dto.PurchaseOrderDto;
import com.database.exception.BusinessException;
import com.database.mapper.*;
import com.database.pojo.Products;
import com.database.pojo.PurchaseOrderItems;
import com.database.pojo.PurchaseOrders;
import com.database.pojo.Suppliers;
import com.database.vo.PurchaseOrderVO;
import com.database.vo.SalesOrderVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PurchaseOrderService {
    private PurchaseOrdersMapper purchaseOrdersMapper;
    private SuppliersMapper suppliersMapper;
    private PurchaseOrderItemsMapper purchaseOrderItemsMapper;
    private ProductsMapper productsMapper;
    private InventoryMapper inventoryMapper;
    @Autowired
    public PurchaseOrderService(PurchaseOrdersMapper purchaseOrdersMapper,
                                SuppliersMapper suppliersMapper,
                                PurchaseOrderItemsMapper purchaseOrderItemsMapper,
                                ProductsMapper productsMapper,
                                InventoryMapper inventoryMapper) {
        this.purchaseOrdersMapper = purchaseOrdersMapper;
        this.suppliersMapper = suppliersMapper;
        this.purchaseOrderItemsMapper = purchaseOrderItemsMapper;
        this.productsMapper = productsMapper;
        this.inventoryMapper = inventoryMapper;
    }
    @Transactional(rollbackFor = Exception.class)
    public PageInfo<PurchaseOrderVO> getPurchaseOrderByPage(int pageNum, int pageSize, String supplierCode, String supplierName, String productCode, String productName, String purchaseOrderCode) {
        PageHelper.startPage(pageNum, pageSize);
        List<PurchaseOrderVO> orderVOList=purchaseOrdersMapper.selectPurchaseOrderVOByPage(supplierCode,supplierName,productCode,productName,purchaseOrderCode);
        return new PageInfo<>(orderVOList);
    }
    /**
     * 2. 创建采购订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void createPurchaseOrder(PurchaseOrderDto dto, Long currentUserId) {
        // 1) 校验供应商是否存在
        Suppliers supplier = suppliersMapper.selectBySupplierCode(dto.getSupplierCode());
        if (supplier == null) {
            throw new BusinessException("错误：供应商编码 " + dto.getSupplierCode() + " 不存在");
        }

        // 2) 生成采购单号 (cg + YYMM + 001)
        String purchaseCode = generatePurchaseCode(dto.getOrderDate());

        // 3) 组装主表 POJO
        PurchaseOrders order = new PurchaseOrders();
        order.setOrderDate(dto.getOrderDate());
        order.setNote(dto.getNote());
        order.setPurchaseCode(purchaseCode);
        order.setSupplierId(supplier.getId());

        // 从供应商表继承负责人信息
        order.setFollowUpPersonId(supplier.getFollowUpPersonId());
        order.setOwnerId(supplier.getOwnerId());

        // 计算总金额 (含税金额)
        BigDecimal totalAmount = calculateTotalAmount(dto.getItems());
        order.setTotalAmount(totalAmount);

        order.setOrderStatus("draft"); // 初始状态为草稿
        order.setCreatedById(currentUserId);
        order.setUpdatedById(currentUserId);

        // 4) 插入数据库
        purchaseOrdersMapper.insertSelective(order);

        // 5) 处理明细
        this.processPurchaseItems(order.getId(), dto.getItems(), currentUserId);
    }

    /**
     * 3. 修改采购订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseOrder(PurchaseOrderDto dto, Long currentUserId) {
        if (dto.getId() == null) throw new RuntimeException("修改操作必须传递 ID");

        PurchaseOrders order = new PurchaseOrders();
        BeanUtils.copyProperties(dto, order);

        // 重新计算总额
        order.setTotalAmount(calculateTotalAmount(dto.getItems()));
        order.setUpdatedById(currentUserId);

        // 更新主表
        purchaseOrdersMapper.updateByPrimaryKeySelective(order);

        // 明细：先删旧，后加新 (典型的课设处理方式)
        purchaseOrderItemsMapper.deleteByPurchaseOrderId(order.getId());
        this.processPurchaseItems(order.getId(), dto.getItems(), currentUserId);
    }

    /**
     * 4. 删除 (软删除)
     */
    public void deletePurchaseOrder(Long id,Long currentUserId) {
        purchaseOrdersMapper.updateToDeleteByPrimaryKey(id,currentUserId);
    }

    /**
     * 处理采购明细
     */
    @Transactional(rollbackFor = Exception.class)
    private void processPurchaseItems(Long orderId, List<PurchaseOrderDto.ItemDTO> items, Long currentUserId) {
        if (items == null || items.isEmpty())
            return;

        for (PurchaseOrderDto.ItemDTO itemDto : items) {
            Products product = productsMapper.selectByProductCode(itemDto.getProductCode());
            if (product == null) throw new BusinessException("产品编码不存在: " + itemDto.getProductCode());

            PurchaseOrderItems detail = new PurchaseOrderItems();
            detail.setPurchaseOrderId(orderId);
            detail.setProductId(product.getId());
            detail.setQuantity(itemDto.getQuantity());
            detail.setUnitPrice(itemDto.getUnitPrice());
            detail.setRemark(itemDto.getRemark());
            detail.setCreatedById(currentUserId);
            detail.setUpdatedById(currentUserId);
            Long productNum = inventoryMapper.selectProductNum(product.getId());
            BigDecimal currentQty = (productNum == null) ? BigDecimal.ZERO : new BigDecimal(productNum);
            BigDecimal inboundQty = itemDto.getQuantity();
            BigDecimal currentCost = product.getCostPrice();
            BigDecimal inboundPrice = itemDto.getUnitPrice();
            // 2. 计算分母：总数量
            BigDecimal totalQty = currentQty.add(inboundQty);
            BigDecimal newCostPrice=inboundPrice;
            // 3. 安全校验：确保总数量大于 0
            log.info(totalQty.toString());
            if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                // 计算总价值
                BigDecimal totalValue = currentCost.multiply(currentQty)
                        .add(inboundPrice.multiply(inboundQty));

                // 计算新成本：保留 4 位小数，使用 HALF_UP（四舍五入）
                // 注意：divide 必须传 scale 和 roundingMode
                newCostPrice = totalValue.divide(totalQty, 4, RoundingMode.HALF_UP);

            }
            log.info(newCostPrice.toString());
            productsMapper.updateProductCostPrice(product.getId(),newCostPrice);

//            detail.setLineTotal(itemDto.getUnitPrice().multiply(itemDto.getQuantity()));
            purchaseOrderItemsMapper.insertSelective(detail);
        }
    }

    /**
     * 计算总额工具方法
     */
    private BigDecimal calculateTotalAmount(List<PurchaseOrderDto.ItemDTO> items) {
        return items.stream()
                .map(item -> item.getQuantity().multiply(item.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 生成单号 cg2505001
     */
    private String generatePurchaseCode(Date orderDate) {
        java.time.LocalDate date = orderDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        String yearMonth = String.format("%02d%02d", date.getYear() % 100, date.getMonthValue());

        // 这里的 SQL 需根据 purchase_code LIKE 'cg2505%' 来统计
        int count = purchaseOrdersMapper.countOrdersByMonth(yearMonth);
        return "cg" + yearMonth + String.format("%03d", count + 1);
    }
}
