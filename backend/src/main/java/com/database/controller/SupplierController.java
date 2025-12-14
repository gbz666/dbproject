package com.database.controller;

import com.database.dto.SupplierDetailDTO;
import com.database.dto.SupplierRequest;
import com.database.pojo.Suppliers;
import com.database.service.SupplierService;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers") // 统一设置基础路径
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * GET /api/suppliers
     * 分页查询供应商列表
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<SupplierDetailDTO>>> getSuppliersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageInfo<SupplierDetailDTO> pageInfo = supplierService.getSuppliersByPage(pageNum, pageSize);

        Result<PageInfo<SupplierDetailDTO>> result = Result.success(pageInfo);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/suppliers
     * 创建新的供应商，接收 currentStaffId 作为请求参数
     */
    @PostMapping
    public ResponseEntity<Result<Suppliers>> createSupplier(
            @RequestBody SupplierRequest request,
            @RequestParam Long currentStaffId) { // <-- 更改点 1: 接收操作员 ID

        // 调用 Service 层方法，传入操作员 ID
        Suppliers newSupplier = supplierService.createSupplier(request, currentStaffId);
        Result<Suppliers> result = Result.success(newSupplier);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/suppliers/{supplierCode}
     * 更新供应商信息，接收 currentStaffId 作为请求参数
     */
    @PutMapping("/{supplierCode}")
    public ResponseEntity<Result<SupplierDetailDTO>> updateSupplier(
            @PathVariable String supplierCode,
            @RequestBody SupplierRequest request,
            @RequestParam Long currentStaffId) { // <-- 更改点 2: 接收操作员 ID

        // 调用 Service 层方法，传入操作员 ID
        SupplierDetailDTO updatedDto = supplierService.updateSupplier(supplierCode, request, currentStaffId);
        Result<SupplierDetailDTO> result = Result.success(updatedDto);
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/suppliers/{supplierCode}
     * 软删除供应商，接收 currentStaffId 作为请求参数
     */
    @DeleteMapping("/{supplierCode}")
    public ResponseEntity<Result<Void>> deleteSupplier(
            @PathVariable String supplierCode,
            @RequestParam Long currentStaffId) { // <-- 更改点 3: 接收操作员 ID

        // 调用 Service 层方法，传入操作员 ID
        supplierService.deleteSupplier(supplierCode, currentStaffId);
        // 删除成功返回 200 OK + 成功 Result
        return ResponseEntity.ok(Result.success("供应商删除成功", null));
    }
}