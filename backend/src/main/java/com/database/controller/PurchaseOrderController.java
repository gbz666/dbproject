package com.database.controller;

import com.database.dto.PurchaseOrderDto;
import com.database.service.PurchaseOrderService;
import com.database.vo.PurchaseOrderVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchaseOrder")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * GET /api/purchaseOrder/page: 分页查询采购订单列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param supplierCode 供应商编码（可选）
     * @param supplierName 供应商名称（可选）
     * @param productCode 产品编码（可选）
     * @param productName 产品名称（可选）
     * @param purchaseOrderCode 采购订单号（可选）
     * @return 200 OK
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<PurchaseOrderVO>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String purchaseOrderCode) {
        PageInfo<PurchaseOrderVO> pageInfo = purchaseOrderService.getPurchaseOrderByPage(
                pageNum, pageSize, supplierCode, supplierName, productCode, productName, purchaseOrderCode);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * POST /api/purchaseOrder: 创建采购订单
     * @param dto 采购订单数据
     * @param currentStaffId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<Void>> create(
            @RequestBody PurchaseOrderDto dto,
            @RequestParam Long currentStaffId) {
        purchaseOrderService.createPurchaseOrder(dto, currentStaffId);
        Result<Void> result = Result.createsuccess(null);
        result.setMessage("创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/purchaseOrder/{id}: 更新采购订单
     * @param id 订单主键ID
     * @param dto 采购订单数据
     * @param currentStaffId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> update(
            @PathVariable Long id,
            @RequestBody PurchaseOrderDto dto,
            @RequestParam Long currentStaffId) {
        dto.setId(id);
        purchaseOrderService.updatePurchaseOrder(dto, currentStaffId);
        Result<Void> result = Result.success("更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/purchaseOrder/{id}: 软删除采购订单
     * @param id 订单主键ID
     * @param currentStaffId 当前操作员ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long currentStaffId) {
        purchaseOrderService.deletePurchaseOrder(id, currentStaffId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
