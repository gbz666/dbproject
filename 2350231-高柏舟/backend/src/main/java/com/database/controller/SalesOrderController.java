package com.database.controller;

import com.database.dto.SalesOrderDTO;
import com.database.service.SalesOrderService;
import com.database.vo.Result;
import com.database.vo.SalesOrderVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salesOrder")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @Autowired
    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    // 1. 分页查询
    @GetMapping
    public ResponseEntity<Result<PageInfo<SalesOrderVO>>> getSalesOrderByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String salesOrderCode) {
        PageInfo<SalesOrderVO> result = salesOrderService.getSalesOrderByPage(
                pageNum, pageSize, customerName, customerCode, productCode, productName,salesOrderCode);
        return ResponseEntity.ok(Result.success(result));
    }

    // 2. 创建订单 (接收 currentUserId)
    @PostMapping
    public ResponseEntity<Result<String>> createSalesOrder(
            @RequestBody SalesOrderDTO salesOrderDTO,
            @RequestParam Long currentUserId) {
        salesOrderService.createSalesOrder(salesOrderDTO, currentUserId);
        return ResponseEntity.ok(Result.success("创建成功"));
    }

    // 3. 修改订单
    @PutMapping("/{id}")
    public ResponseEntity<Result<String>> updateSalesOrder(
            @PathVariable Long id,
            @RequestBody SalesOrderDTO salesOrderDTO,
            @RequestParam Long currentUserId) {
        salesOrderDTO.setId(id);
        salesOrderService.updateSalesOrder(salesOrderDTO, currentUserId);
        return ResponseEntity.ok(Result.success("修改成功"));
    }

    // 4. 删除订单 (软删除)
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok(Result.success("删除成功"));
    }
}