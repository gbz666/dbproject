package com.database.controller;

import com.database.dto.CustomerDetailDTO;
import com.database.dto.CustomerCreateRequest; // 引入用于创建的 DTO
import com.database.dto.CustomerUpdateRequest;
import com.database.pojo.Customers;
import com.database.service.CustomerService;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // 引入 HTTP 状态码
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers") // 统一路径前缀
public class CustomerController {

    // 推荐使用构造器注入和 final 关键字
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * GET /api/customers: 分页查询客户列表
     * @return 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<CustomerDetailDTO>>> getCustomersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageInfo<CustomerDetailDTO> pageInfo = customerService.getCustomersByPage(pageNum, pageSize);

        // 状态码：200 OK
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * POST /api/customers: 创建新客户
     * @param request 包含客户基础信息和关联员工姓名的请求 DTO
     * @param currentStaffId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<Customers>> createCustomer(
            @RequestBody CustomerCreateRequest request, // 使用更贴合语义的 CustomerCreateRequest
            @RequestParam Long currentStaffId) {

        Customers newCustomer = customerService.createCustomer(request, currentStaffId);

        // 成功响应：RESTful 最佳实践返回 201 Created
        Result<Customers> result = Result.createsuccess(newCustomer);
        result.setMessage("客户创建成功");

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * DELETE /api/customers/{customerCode}: 软删除客户
     * @param customerCode 客户业务编号
     * @param currentStaffId 当前操作员ID
     * @return 204 No Content
     */
    @DeleteMapping("/{customerCode}")
    public ResponseEntity<Result<Void>> deleteCustomer(
            @PathVariable String customerCode,
            @RequestParam Long currentStaffId) {

        customerService.deleteCustomer(customerCode, currentStaffId);

        // 成功响应：RESTful 最佳实践返回 204 No Content（删除成功且不返回 Body）
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    /**
     * PUT /api/customers/{customerCode}：更新客户信息
     * @param customerCode 客户业务编号 (Path 路径)
     * @param customerDTO 待更新的客户数据（使用 CustomerDetailDTO，Service 层需处理转换）
     * @param currentStaffId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/{customerCode}")
    public ResponseEntity<Result<CustomerDetailDTO>> updateCustomer(
            @PathVariable String customerCode,
            @RequestBody CustomerUpdateRequest customerDTO,
            @RequestParam Long currentStaffId) {

        CustomerDetailDTO updatedCustomer = customerService.updateCustomer(customerCode, customerDTO, currentStaffId);

        // 构造成功响应：200 OK
        Result<CustomerDetailDTO> result = Result.success(updatedCustomer);
        result.setMessage("客户信息更新成功");

        return ResponseEntity.ok(result);
    }
}