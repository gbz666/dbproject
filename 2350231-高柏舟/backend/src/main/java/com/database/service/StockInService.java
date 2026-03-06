package com.database.service;
import com.database.dto.StockInDTO; // 需根据前端结构创建对应的 DTO
import com.database.mapper.*;
import com.database.pojo.StockInItems;
import com.database.pojo.StockIns;
import com.database.vo.StockInVO;
import com.database.vo.WarehouseStockVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class StockInService {

    private final StockInsMapper stockInMapper;
    private final ProductsMapper productsMapper;
    private final StockInItemsMapper stockInItemMapper;
    private final InventoryMapper inventoryMapper;
    private final PurchaseOrdersMapper purchaseOrdersMapper;

    @Autowired
    public StockInService(StockInsMapper stockInMapper,
                          ProductsMapper productsMapper,
                          StockInItemsMapper stockInItemMapper,
                          InventoryMapper inventoryMapper,
                          PurchaseOrdersMapper purchaseOrdersMapper) {
        this.stockInMapper = stockInMapper;
        this.productsMapper = productsMapper;
        this.stockInItemMapper = stockInItemMapper;
        this.inventoryMapper = inventoryMapper;
        this.purchaseOrdersMapper = purchaseOrdersMapper;
    }

    /**
     * 创建入库：插入入库单并增加库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void createStockIn(StockInDTO dto, Long operatorId) {
        // 1. 组装实体类
        StockIns stockIn = new StockIns();
        // 通过采购订单编号查询 ID (假设 purchaseOrdersMapper 有此方法)
        Long purchaseOrderId = purchaseOrdersMapper.selectOrderIdByCode(dto.getPurchaseOrderCode());

        stockIn.setPurchaseOrderId(purchaseOrderId);
        stockIn.setStockInDate(dto.getStockInDate());
        stockIn.setNote(dto.getNote());
        stockIn.setCreatedById(operatorId);
        stockIn.setUpdatedById(operatorId);

        // 插入主表，回填 ID
        stockInMapper.insertSelective(stockIn);

        // 2. 处理明细与库存增加
        processItemsAndInventory(stockIn.getId(), dto, operatorId);
    }

    /**
     * 修改入库：先回退旧库存，再重新插入新数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStockIn(StockInDTO dto, Long operatorId) {
        // 1. 还原旧库存：入库单修改前，需要把之前增加的库存减掉
        restoreOldInventory(dto.getId());

        // 2. 删除旧明细记录
        stockInItemMapper.deleteByStockInId(dto.getId());

        // 3. 更新主表
        StockIns stockIn = new StockIns();
        stockIn.setId(dto.getId());
        stockIn.setStockInDate(dto.getStockInDate());
        stockIn.setNote(dto.getNote());
        stockIn.setUpdatedById(operatorId);
        stockInMapper.updateByPrimaryKeySelective(stockIn);

        // 4. 插入新明细并重新增加库存
        processItemsAndInventory(dto.getId(), dto, operatorId);
    }

    /**
     * 删除入库：回退库存并软删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockIn(Long id, Long operatorId) {
        // 1. 回退库存（减去入库数量）
        restoreOldInventory(id);
        // 2. 软删除主单据
        stockInMapper.softDeleteStockIn(id, operatorId);
    }

    /**
     * 内部方法：回退旧库存（入库的回退是做减法）
     */
    private void restoreOldInventory(Long stockInId) {
        List<StockInItems> items = stockInItemMapper.selectByStockInId(stockInId);
        for (StockInItems item : items) {
            // 入库单原本是加库存，回退时传入负数减掉
            inventoryMapper.updateInventory(item.getProductId(), item.getWarehouseId(), item.getQuantity().negate());
        }
    }

    /**
     * 内部方法：处理明细插入与库存增加
     */
    private void processItemsAndInventory(Long stockInId, StockInDTO dto, Long operatorId) {
        for (StockInDTO.StockInItemDTO itemDto : dto.getItems()) {
            // 获取产品 ID
            Long productId = productsMapper.selectByProductCode(itemDto.getProductCode()).getId();

            for (WarehouseStockVO wh : itemDto.getWarehouseDetails()) {
                // 1. 插入入库明细
                stockInItemMapper.insertItemDetail(
                        stockInId,
                        productId,
                        wh.getWarehouseId(),
                        wh.getQuantity(),
                        itemDto.getSerialNumbers(),
                        operatorId
                );

                // 2. 更新库存：入库是增加库存，传入正数
                inventoryMapper.updateInventory(productId, wh.getWarehouseId(), wh.getQuantity());
            }
        }
    }

    /**
     * 分页查询入库明细
     */
    public PageInfo<StockInVO> getStockInByPage(
            int pageNum, int pageSize, String purchaseOrderCode, String supplierName,
            String productName,String serialNumber) {

        PageHelper.startPage(pageNum, pageSize);

        List<StockInVO> list = stockInMapper.selectStockInDetails(
                purchaseOrderCode, supplierName, productName,serialNumber);

        // 计算小计逻辑（参考出库单）
        list.forEach(vo -> {
            if (vo.getWarehouseDetails() != null) {
                BigDecimal subTotal = vo.getWarehouseDetails().stream()
                        .map(WarehouseStockVO::getQuantity)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setSubTotal(subTotal);
                // 这里的逻辑可以根据采购单欠交数量调整
                if(vo.getPurchaseQuantity() != null) {
                    vo.setPendingQuantity(vo.getPurchaseQuantity().subtract(subTotal));
                }
            }
        });

        return new PageInfo<>(list);
    }
}