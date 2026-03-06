package com.database.controller;

import com.database.dto.ProductRequest;
import com.database.service.ProductsService;
import com.database.vo.ProductVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    private final ProductsService productsService;

    @Autowired
    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    /**
     * GET /api/products: 分页查询产品列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param productName 产品名称（可选）
     * @param categoryName 分类名称（可选）
     * @param productCode 产品编码（可选）
     * @param productType 产品类型（可选）
     * @return 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<ProductVO>>> getProductsByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productType) {
        PageInfo<ProductVO> pageInfo = productsService.getProductsByPage(
                pageNum, pageSize, productName, categoryName, productCode, productType);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * GET /api/products/{productCode}: 根据业务编码获取产品详情
     * @param productCode 产品业务编码
     * @return 200 OK
     */
    @GetMapping("/{productCode}")
    public ResponseEntity<Result<ProductVO>> getProduct(@PathVariable String productCode) {
        ProductVO product = productsService.getProductDetail(productCode);
        return ResponseEntity.ok(Result.success(product));
    }

    /**
     * POST /api/products: 创建产品（产品编码在 Service 层生成）
     * @param request 产品请求数据
     * @param currentStaffId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<ProductVO>> createProduct(
            @RequestBody ProductRequest request,
            @RequestParam Long currentStaffId) {
        ProductVO newProduct = productsService.createProduct(request, currentStaffId);
        Result<ProductVO> result = Result.createsuccess(newProduct);
        result.setMessage("产品创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/products/{productCode}: 根据业务编码修改产品
     * @param productCode 产品业务编码
     * @param request 产品请求数据
     * @param currentStaffId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/{productCode}")
    public ResponseEntity<Result<ProductVO>> updateProduct(
            @PathVariable String productCode,
            @RequestBody ProductRequest request,
            @RequestParam Long currentStaffId) {
        ProductVO updatedProduct = productsService.updateProduct(productCode, request, currentStaffId);
        Result<ProductVO> result = Result.success(updatedProduct);
        result.setMessage("产品更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/products/{productCode}: 逻辑删除产品
     * @param productCode 产品业务编码
     * @return 204 No Content
     */
    @DeleteMapping("/{productCode}")
    public ResponseEntity<Result<Void>> deleteProduct(@PathVariable String productCode) {
        productsService.deleteProduct(productCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
