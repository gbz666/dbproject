package com.database.service;

import com.database.dto.OutboundOrderDTO;
import com.database.exception.BusinessException;
import com.database.mapper.*;
import com.database.pojo.OutboundOrderItems;
import com.database.pojo.OutboundOrders;
import com.database.vo.OutboundDetailVO;
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
public class OutBoundOrdersService {

    private OutboundOrdersMapper outboundMapper;
    private ProductsMapper productsMapper;
    private OutboundOrderItemsMapper itemMapper;
    private InventoryMapper inventoryMapper;
    private SalesOrdersMapper salesOrdersMapper;
    @Autowired
    OutBoundOrdersService(OutboundOrdersMapper outboundMapper,
                          ProductsMapper productsMapper,
                          OutboundOrderItemsMapper outboundOrderItemsMapper,
                          InventoryMapper inventoryMapper,
                          SalesOrdersMapper salesOrdersMapper) {
        this.outboundMapper = outboundMapper;
        this.productsMapper = productsMapper;

        this.itemMapper = outboundOrderItemsMapper;
        this.inventoryMapper = inventoryMapper;
        this.salesOrdersMapper = salesOrdersMapper;
    }
    /**
     * 创建逻辑：直接插入并扣减库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOutbound(OutboundOrderDTO dto, Long operatorId) {
        // 1. 组装实体类并插入
        OutboundOrders outboundOrder = new OutboundOrders();
        Long id = salesOrdersMapper.selectOrderIdByCode(dto.getSalesOrderCode());
        outboundOrder.setSalesOrderId(id); // 必须设置，关联来源
        outboundOrder.setOutboundDate(dto.getOutboundDate());
        outboundOrder.setRemark(dto.getRemark());
        outboundOrder.setCreatedById(operatorId); // 记录操作人
        outboundOrder.setUpdatedById(operatorId);

        // 执行插入。执行后，MyBatis 会自动将生成的主键回填到 outboundOrder 对象的 id 属性中
        outboundMapper.insertSelective(outboundOrder);

        // 2. 将生成的主表 ID 传递给明细处理函数
        processItemsAndInventory(outboundOrder.getId(), dto, operatorId);
    }

    /**
     * 修改逻辑：先还原库存，再重新插入
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOutbound(OutboundOrderDTO dto, Long operatorId) {
        // 1. 还原旧库存：通过 itemMapper 查询当前数据库里的明细，并加回库存
        restoreOldInventory(dto.getId());

        // 2. 删除旧明细记录：根据主表 ID 物理删除明细表数据
        // 注意：这里调用的是 itemMapper，而不是主表的 deleteByPrimaryKey
        itemMapper.deleteByOutboundOrderId(dto.getId());

        // 3. 更新主表：更新日期、备注、修改人 ID 等
        OutboundOrders outboundOrder = new OutboundOrders();
        outboundOrder.setId(dto.getId());
        outboundOrder.setOutboundDate(dto.getOutboundDate());
        outboundOrder.setRemark(dto.getRemark());
        outboundOrder.setUpdatedById(operatorId);
        outboundMapper.updateByPrimaryKeySelective(outboundOrder);

        // 4. 插入新明细并重新扣减库存
        // 传入主表 ID dto.getId() 确保明细关联正确
        processItemsAndInventory(dto.getId(), dto, operatorId);
    }

    private void restoreOldInventory(Long orderId) {
        // 使用 itemMapper 获取该单据下的所有旧明细
        List<OutboundOrderItems> items = itemMapper.selectByOutboundOrderId(orderId);
        for (OutboundOrderItems item : items) {
            // 将旧出库数量加回库存
            inventoryMapper.updateInventory(item.getProductId(), item.getWarehouseId(), item.getQuantity());
        }
    }

    private void processItemsAndInventory(Long orderId, OutboundOrderDTO dto, Long operatorId) {
        for (OutboundOrderDTO.OutboundItemDTO itemDto : dto.getItems()) {
            // 1. 获取产品 ID
            Long productId = productsMapper.selectByProductCode(itemDto.getProductCode()).getId();

            for (WarehouseStockVO wh : itemDto.getWarehouseDetails()) {
                // 2. 使用 itemMapper 插入明细表记录
                // 注意：此时 orderId 是从主表 insertSelective 之后拿到的自增 ID
                itemMapper.insertItemDetail(
                        orderId,
                        productId,
                        wh.getWarehouseId(),
                        wh.getQuantity(),
                        itemDto.getSerialNumbers(),
                        operatorId
                );

                // 3. 使用 outboundMapper (或专门的库存Mapper) 更新库存
                // 出库是减少库存，所以传入负数
                inventoryMapper.updateInventory(productId, wh.getWarehouseId(), wh.getQuantity().negate());
            }
        }
    }



    @Transactional
    public void deleteOutbound(Long id, Long operatorId) {
        restoreOldInventory(id);
        outboundMapper.softDeleteOrder(id, operatorId);
    }

    public PageInfo<OutboundDetailVO> getOutboundByPage(
            int pageNum, int pageSize, String salesOrderCode, String customerName,
            String productName, String serialNumber) {

        // 1. 开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 执行查询 (MyBatis 会自动拼接 limit)
        List<OutboundDetailVO> list = outboundMapper.selectOutboundDetails(
                salesOrderCode, customerName, productName, serialNumber);

        // 3. 内存中处理 VO 计算属性
        list.forEach(vo -> {
            if (vo.getWarehouseDetails() != null) {
                BigDecimal subTotal = vo.getWarehouseDetails().stream()
                        .map(WarehouseStockVO::getQuantity)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setSubTotal(subTotal);
                vo.setPendingQuantity(vo.getOrderQuantity().subtract(subTotal));
            }
        });

        return new PageInfo<>(list);
    }

}
