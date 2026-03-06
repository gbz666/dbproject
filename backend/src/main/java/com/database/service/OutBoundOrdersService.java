package com.database.service;

import com.database.dto.OutboundOrderDTO;
import com.database.exception.BusinessException;
import com.database.mapper.*;
import com.database.pojo.OutboundOrderItems;
import com.database.pojo.OutboundOrders;
import com.database.vo.OutboundDetailVO;
import com.database.vo.WarehouseStockVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
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
                // 1. 获取原始字符串
                String sns = itemDto.getSerialNumbers();
                String jsonSns = null; // 默认为 null，对应数据库 NULL

                if (sns != null && !sns.trim().isEmpty()) {
                    // 2. 将逗号分隔的字符串转为 JSON 数组格式
                    // 例如: "A,B" -> ["A","B"]
                    String[] snArray = sns.split(",");
                    // 使用 Jackson 序列化（推荐）
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        jsonSns = mapper.writeValueAsString(snArray);
                    } catch (Exception e) {
                        jsonSns = "[]"; // 出错则传空数组
                    }
                }

// 3. 传入转换后的 jsonSns
                itemMapper.insertItemDetail(
                        orderId,
                        productId,
                        wh.getWarehouseId(),
                        wh.getQuantity(),
                        jsonSns, // 这里传处理后的合法 JSON 串或 null
                        operatorId
                );

                // 3. 原子扣减库存（高并发防超卖：仅当库存足够时扣减，否则抛异常回滚）
                int updated = inventoryMapper.deductInventoryIfSufficient(productId, wh.getWarehouseId(), wh.getQuantity());
                if (updated == 0) {
                    throw new BusinessException("库存不足，无法出库。产品ID=" + productId + ", 仓库ID=" + wh.getWarehouseId() + ", 需要数量=" + wh.getQuantity());
                }
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
    @Transactional(rollbackFor = Exception.class)
    public void initializeEmptyOutbound(Long salesOrderId, Long operatorId) {
        OutboundOrders outboundOrder = new OutboundOrders();
        outboundOrder.setSalesOrderId(salesOrderId);
        // 默认出库日期为当前日期或留空
        outboundOrder.setOutboundDate(new Date());
        outboundOrder.setRemark("销售订单自动生成，待执行出库");
        outboundOrder.setCreatedById(operatorId);
        outboundOrder.setUpdatedById(operatorId);
        outboundOrder.setStatus("DRAFT"); // 初始状态为草稿

        // 插入主表。此时不需要处理 Items 和 Inventory，因为出库数量为 0
        outboundMapper.insertSelective(outboundOrder);
    }
}
