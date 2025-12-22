package com.database.service;

import com.database.dto.PurchaseOrderDto;
import com.database.exception.BusinessException;
import com.database.mapper.*;
import com.database.pojo.*;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PurchaseOrderService {
    private PurchaseOrdersMapper purchaseOrdersMapper;
    private SuppliersMapper suppliersMapper;
    private PurchaseOrderItemsMapper purchaseOrderItemsMapper;
    private ProductsMapper productsMapper;
    private InventoryMapper inventoryMapper;
    private StockInsMapper stockInsMapper;
    @Autowired
    public PurchaseOrderService(PurchaseOrdersMapper purchaseOrdersMapper,
                                SuppliersMapper suppliersMapper,
                                PurchaseOrderItemsMapper purchaseOrderItemsMapper,
                                ProductsMapper productsMapper,
                                InventoryMapper inventoryMapper,
                                StockInsMapper stockInsMapper) {
        this.purchaseOrdersMapper = purchaseOrdersMapper;
        this.suppliersMapper = suppliersMapper;
        this.purchaseOrderItemsMapper = purchaseOrderItemsMapper;
        this.productsMapper = productsMapper;
        this.inventoryMapper = inventoryMapper;
        this.stockInsMapper = stockInsMapper;
    }
    @Transactional(rollbackFor = Exception.class)
    public PageInfo<PurchaseOrderVO> getPurchaseOrderByPage(
            int pageNum, int pageSize, String supplierCode, String supplierName,
            String productCode, String productName, String purchaseOrderCode) {

        // 1. 启动分页 (只对下面的第一条 SQL 有效)
        PageHelper.startPage(pageNum, pageSize);

        // 2. 查主表：此时 orderVOList 的长度等于 pageSize，且没有重复记录
        List<PurchaseOrderVO> orderVOList = purchaseOrdersMapper.selectMainOrderPage(
                supplierCode, supplierName, productCode, productName, purchaseOrderCode
        );

        if (orderVOList == null || orderVOList.isEmpty()) {
            return new PageInfo<>(new ArrayList<>());
        }

        // 3. 收集当前页所有订单 ID
        List<Long> orderIds = orderVOList.stream()
                .map(PurchaseOrderVO::getId)
                .collect(Collectors.toList());

        // 4. 查明细：一次性查出当前页所有订单的全部商品
        List<PurchaseOrderVO.OrderItemDTO> allItems = purchaseOrdersMapper.selectItemsByOrderIds(orderIds);

        // 5. 分组：将明细按订单 ID 分组存储在 Map 里
        Map<Long, List<PurchaseOrderVO.OrderItemDTO>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(PurchaseOrderVO.OrderItemDTO::getOrderId));

        // 6. 回填：遍历主表列表，把对应的明细塞进去
        orderVOList.forEach(order -> {
            order.setItems(itemMap.getOrDefault(order.getId(), new ArrayList<>()));
        });

        // 7. 封装返回（PageHelper 会保留正确的 TotalCount）
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
        createAutoStockIn( order, currentUserId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void  createAutoStockIn(PurchaseOrders order, Long currentUserId) {
        // 1) 校验供应商并生成单号 (略)

        StockIns stockIn = new StockIns();
        stockIn.setPurchaseOrderId(order.getId());
        stockIn.setStockInCode("IN-" + order.getPurchaseCode()); // 业务关联
        stockIn.setStockInDate(null); // 此时还没入库，日期可为空或设为预定日期
        stockIn.setCreatedById(currentUserId);
        stockInsMapper.insertSelective(stockIn);

        // 注意：这里不需要插入 stock_in_items，也不需要更新 inventory！
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
    public void processPurchaseItems(Long orderId, List<PurchaseOrderDto.ItemDTO> items, Long currentUserId) {
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
