package com.database.controller;

import com.database.dto.PurchaseOrderDto;
import com.database.service.PurchaseOrderService;
import com.database.vo.PurchaseOrderVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchaseOrder")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
     * 分页查询采购订单
     */
    @GetMapping("/page")
    public Result<PageInfo<PurchaseOrderVO>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String purchaseOrderCode) {

        PageInfo<PurchaseOrderVO> pageInfo = purchaseOrderService.getPurchaseOrderByPage(
                pageNum, pageSize, supplierCode, supplierName, productCode, productName, purchaseOrderCode);
        return Result.success(pageInfo);
    }

    /**
     * 新增采购订单
     */
    @PostMapping
    public Result<String> create(@RequestBody PurchaseOrderDto dto,@RequestParam Long currentUserId) {
        purchaseOrderService.createPurchaseOrder(dto, currentUserId);
        return Result.success("创建成功");
    }

    /**
     * 更新采购订单
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody PurchaseOrderDto dto,@RequestParam Long currentUserId) {
        dto.setId(id);
        purchaseOrderService.updatePurchaseOrder(dto, currentUserId);
        return Result.success("更新成功");
    }

    /**
     * 软删除采购订单
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestParam Long currentUserId) {
        purchaseOrderService.deletePurchaseOrder(id,currentUserId);
        return Result.success("删除成功");
    }
}
