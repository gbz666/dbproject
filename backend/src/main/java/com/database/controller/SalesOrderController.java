package com.database.controller;

import com.database.dto.SalesOrderDTO;
import com.database.service.SalesOrderService;
import com.database.vo.Result;
import com.database.vo.SalesOrderVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    /**
     * GET /api/salesOrder: 分页查询销售订单列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param customerName 客户名称（可选）
     * @param customerCode 客户编码（可选）
     * @param productCode 产品编码（可选）
     * @param productName 产品名称（可选）
     * @param salesOrderCode 销售订单号（可选）
     * @return 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<SalesOrderVO>>> getSalesOrderByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String salesOrderCode) {
        PageInfo<SalesOrderVO> pageInfo = salesOrderService.getSalesOrderByPage(
                pageNum, pageSize, customerName, customerCode, productCode, productName, salesOrderCode);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * POST /api/salesOrder: 创建销售订单
     * @param salesOrderDTO 销售订单数据
     * @param currentStaffId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<Void>> createSalesOrder(
            @RequestBody SalesOrderDTO salesOrderDTO,
            @RequestParam Long currentStaffId) {
        salesOrderService.createSalesOrder(salesOrderDTO, currentStaffId);
        Result<Void> result = Result.createsuccess(null);
        result.setMessage("创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/salesOrder/{id}: 修改销售订单
     * @param id 订单主键ID
     * @param salesOrderDTO 销售订单数据
     * @param currentStaffId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateSalesOrder(
            @PathVariable Long id,
            @RequestBody SalesOrderDTO salesOrderDTO,
            @RequestParam Long currentStaffId) {
        salesOrderDTO.setId(id);
        salesOrderService.updateSalesOrder(salesOrderDTO, currentStaffId);
        Result<Void> result = Result.success("修改成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/salesOrder/{id}: 软删除销售订单
     * @param id 订单主键ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
