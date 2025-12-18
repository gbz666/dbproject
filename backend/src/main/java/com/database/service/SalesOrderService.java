package com.database.service;

import com.database.dto.SalesOrderDTO;
import com.database.mapper.CustomersMapper;
import com.database.mapper.ProductsMapper;
import com.database.mapper.SalesOrderItemsMapper;
import com.database.mapper.SalesOrdersMapper;
import com.database.pojo.Customers;
import com.database.pojo.Products;
import com.database.pojo.SalesOrderItems;
import com.database.pojo.SalesOrders;
import com.database.vo.CustomerDetailVO;
import com.database.vo.SalesOrderVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class SalesOrderService {
    // 注入所有必要的 Mapper
    private final SalesOrdersMapper salesOrdersMapper;
    private final SalesOrderItemsMapper itemsMapper;
    private final CustomersMapper customerMapper;
    private final ProductsMapper productMapper;

    @Autowired // 使用构造器注入
    public SalesOrderService(SalesOrdersMapper salesOrdersMapper,
                             SalesOrderItemsMapper itemsMapper,
                             CustomersMapper customerMapper,
                             ProductsMapper productMapper) {
        this.salesOrdersMapper = salesOrdersMapper;
        this.itemsMapper = itemsMapper;
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
    }

    // 1. 查询依然返回 VO
    public PageInfo<SalesOrderVO> getSalesOrderByPage(int pageNum, int pageSize, String customerName, String customerCode, String productCode, String productName, String salesOrderCode) {
        PageHelper.startPage(pageNum, pageSize);
        List<SalesOrderVO> salesOrderList = salesOrdersMapper.selectSalesOrderByPage(customerName, customerCode, productCode, productName,salesOrderCode);
        return new PageInfo<>(salesOrderList);
    }

    // 2. 创建订单 (参数改为 DTO)
    @Transactional(rollbackFor = Exception.class)
    public void createSalesOrder(SalesOrderDTO dto, Long currentUserId) {
        // 1) 校验客户
        Customers customer = customerMapper.selectByCustomerCode(dto.getCustomerCode());
        if (customer == null) {
            throw new RuntimeException("错误：客户编码 " + dto.getCustomerCode() + " 不存在");
        }

        // 2) 生成单号 (XS + YYMM + 001)
        String orderCode = generateOrderCode(dto.getOrderDate());

        // 3) 组装订单主表
        SalesOrders order = new SalesOrders();
        BeanUtils.copyProperties(dto, order); // 将 DTO 中的日期、备注拷贝到 POJO

        order.setOrderCode(orderCode);
        order.setCustomerId(customer.getId());
        // 从客户表同步默认的人员配置
        order.setSalesPersonId(customer.getSalesPersonId());
        order.setFollowUpPersonId(customer.getFollowUpPersonId());
        order.setOwnerId(customer.getOwnerId());
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderDTO.ItemDTO item : dto.getItems()) {
            // 计算单行金额：数量 * 单价
            BigDecimal itemTotal = item.getQuantity().multiply(item.getUnitPrice());
            // 累加到总金额
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);
        order.setCreatedById(currentUserId);
        order.setUpdatedById(currentUserId);
        order.setIsDeleted(0);
        order.setOrderStatus("PENDING"); // 初始状态

        // 4) 插入主表，回填 ID
        salesOrdersMapper.insertSelective(order);

        // 5) 处理明细 (使用 DTO 里的 items)
        this.processItems(order.getId(), dto.getItems(),currentUserId);
    }

    // 3. 修改订单 (参数改为 DTO)
    @Transactional(rollbackFor = Exception.class)
    public void updateSalesOrder(SalesOrderDTO dto, Long currentUserId) {
        SalesOrders order = new SalesOrders();
        BeanUtils.copyProperties(dto, order);
        order.setUpdatedById(currentUserId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesOrderDTO.ItemDTO item : dto.getItems()) {
            // 计算单行金额：数量 * 单价
            BigDecimal itemTotal = item.getQuantity().multiply(item.getUnitPrice());
            // 累加到总金额
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);
        // 更新主表
        salesOrdersMapper.updateByPrimaryKeySelective(order);

        // 明细：先删旧，后加新
        itemsMapper.deleteByOrderId(order.getId());
        this.processItems(order.getId(), dto.getItems(),currentUserId);
    }

    // 4. 删除
    public void deleteSalesOrder(Long id) {
        salesOrdersMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改后的明细处理方法，参数对应 SalesOrderDTO.ItemDTO
     */
    private void processItems(Long orderId, List<SalesOrderDTO.ItemDTO> items,Long currentUserId) {
        if (items == null || items.isEmpty()) return;
        for (SalesOrderDTO.ItemDTO itemDto : items) {
            Products product = productMapper.selectByProductCode(itemDto.getProductCode());
            if (product == null) throw new RuntimeException("产品编码不存在: " + itemDto.getProductCode());

            SalesOrderItems detail = new SalesOrderItems();
            detail.setSalesOrderId(orderId);
            detail.setProductId(product.getId());
            detail.setQuantity(itemDto.getQuantity());
            detail.setUnitPrice(itemDto.getUnitPrice());
            detail.setCreatedById(currentUserId);
            // 自动从产品表锁定当前成本
            detail.setCostPrice(product.getCostPrice());
            itemsMapper.insert(detail);
        }
    }

    private String generateOrderCode(Date orderDate) {
        // 1. 将 java.util.Date 转为 java.time.LocalDate 方便提取年月
        java.time.Instant instant = orderDate.toInstant();
        java.time.LocalDate date = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        // 2. 提取年份后两位和月份（确保月份是2位，如05）
        String yearMonth = String.format("%02d%02d", date.getYear() % 100, date.getMonthValue());

        // 3. 查询数据库中该月份已有的订单数（SQL 逻辑保持不变：LIKE 'XS2505%'）
        int count = salesOrdersMapper.countOrdersByMonth(yearMonth);

        // 4. 序号递增并补零
        String sequence = String.format("%03d", count + 1);

        return "xs" + yearMonth + sequence;
    }
}