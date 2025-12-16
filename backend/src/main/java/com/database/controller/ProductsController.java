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
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    @Autowired
    private ProductsService productsService;


    /** * [C] 创建产品
     * POST /api/products
     * 成功返回: 201 Created
     * 【注意】创建时产品编码在 Service 层生成。
     */
    @PostMapping
    public ResponseEntity<Result<ProductVO>> createProduct(
            @RequestBody ProductRequest request,
            @RequestParam Long currentStaffId) {

        // 1. 调用 Service
        ProductVO newProduct = productsService.createProduct(request, currentStaffId);

        // 2. 封装 Result (使用 createsuccess，code=201)
        Result<ProductVO> result = Result.createsuccess(newProduct);

        // 3. 返回 ResponseEntity，状态码设置为 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /** * [U] 修改产品 (使用业务编码进行定位)
     * PUT /api/products/{productCode}
     * 成功返回: 200 OK
     */
    @PutMapping("/{productCode}")
    public ResponseEntity<Result<ProductVO>> updateProduct(
            @PathVariable String productCode, // 接收业务编码
            @RequestBody ProductRequest request,
            @RequestParam Long currentStaffId) {

        // 1. 调用 Service，传递 productCode
        ProductVO updatedProduct = productsService.updateProduct(productCode, request, currentStaffId);

        // 2. 封装 Result (使用 success，code=200)
        Result<ProductVO> result = Result.success(updatedProduct);

        // 3. 返回 ResponseEntity，状态码设置为 200 OK
        return ResponseEntity.ok(result);
    }

    /** * [R] 获取产品详情 (使用业务编码)
     * GET /api/products/{productCode}
     * 成功返回: 200 OK
     */
    @GetMapping("/{productCode}")
    public ResponseEntity<Result<ProductVO>> getProduct(@PathVariable String productCode) {

        // 1. 调用 Service，传递 productCode
        ProductVO product = productsService.getProductDetail(productCode);

        // 2. 封装 Result (使用 success，code=200)
        Result<ProductVO> result = Result.success(product);

        // 3. 返回 ResponseEntity，状态码设置为 200 OK
        return ResponseEntity.ok(result);
    }



    /** * [D] 逻辑删除产品 (使用业务编码)
     * DELETE /api/products/{productCode}
     * 成功返回: 204 No Content
     */
    @DeleteMapping("/{productCode}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productCode) {

        // 1. 调用 Service，传递 productCode
        productsService.deleteProduct(productCode);

        // 2. 删除成功，返回 204 No Content，不含响应体
        return ResponseEntity.noContent().build();
    }
    /** * [R] 获取产品列表
     * GET /api/products?productName=xxx&categoryName=yyy
     * 成功返回: 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<ProductVO>>> getProductsByPage(
            @RequestParam(defaultValue = "1") int pageNum, // 默认页码 1
            @RequestParam(defaultValue = "10") int pageSize, // 默认每页 10 条
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String productCode) {

        // 1. 调用 Service 分页查询
        PageInfo<ProductVO> pageInfo = productsService.getProductsByPage(
                pageNum,
                pageSize,
                productName,
                categoryName,
                productCode
        );

        // 2. 封装 Result (使用 success，code=200)
        Result<PageInfo<ProductVO>> result = Result.success(pageInfo);

        // 3. 返回 ResponseEntity，状态码设置为 200 OK
        return ResponseEntity.ok(result);
    }
}