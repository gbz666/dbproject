package com.database.controller;

import com.database.dto.SupplierRequest;
import com.database.pojo.Suppliers;
import com.database.service.SupplierService;
import com.database.vo.Result;
import com.database.vo.SupplierDetailVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * GET /api/suppliers: 分页查询供应商列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param supplierCode 供应商编码（可选）
     * @param supplierName 供应商名称（可选）
     * @return 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<SupplierDetailVO>>> getSuppliersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName) {
        PageInfo<SupplierDetailVO> pageInfo = supplierService.getSuppliersByPage(
                pageNum, pageSize, supplierCode, supplierName);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * POST /api/suppliers: 创建供应商
     * @param request 供应商请求数据
     * @param currentStaffId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<Suppliers>> createSupplier(
            @RequestBody SupplierRequest request,
            @RequestParam Long currentStaffId) {
        Suppliers newSupplier = supplierService.createSupplier(request, currentStaffId);
        Result<Suppliers> result = Result.createsuccess(newSupplier);
        result.setMessage("供应商创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/suppliers/{supplierCode}: 更新供应商信息
     * @param supplierCode 供应商业务编码
     * @param request 供应商请求数据
     * @param currentStaffId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/{supplierCode}")
    public ResponseEntity<Result<SupplierDetailVO>> updateSupplier(
            @PathVariable String supplierCode,
            @RequestBody SupplierRequest request,
            @RequestParam Long currentStaffId) {
        SupplierDetailVO updatedDto = supplierService.updateSupplier(supplierCode, request, currentStaffId);
        Result<SupplierDetailVO> result = Result.success(updatedDto);
        result.setMessage("供应商更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/suppliers/{supplierCode}: 软删除供应商
     * @param supplierCode 供应商业务编码
     * @param currentStaffId 当前操作员ID
     * @return 204 No Content
     */
    @DeleteMapping("/{supplierCode}")
    public ResponseEntity<Result<Void>> deleteSupplier(
            @PathVariable String supplierCode,
            @RequestParam Long currentStaffId) {
        supplierService.deleteSupplier(supplierCode, currentStaffId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
