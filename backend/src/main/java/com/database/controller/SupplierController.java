package com.database.controller;

import com.database.pojo.Customers;
import com.database.pojo.Suppliers;
import com.database.service.SupplierService;
import com.database.vo.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SupplierController {
    SupplierService supplierService;
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }
    @GetMapping("/api/suppliers")
    public ResponseEntity<Result<PageInfo<Suppliers>>> getCustomersByPage(@RequestParam(defaultValue = "1") int pageNum,
                                                                            @RequestParam(defaultValue = "10") int pageSize) {

        // 1. 调用 Service 层获取分页数据
        PageInfo<Suppliers> pageInfo = supplierService.getSuppliersByPage(pageNum, pageSize);

        // 2. 使用 Result.success() 封装 PageInfo 对象
        Result<PageInfo<Suppliers>> result = Result.success(pageInfo);

        // 3. 返回 ResponseEntity。这里 HTTP 状态码通常设置为 200 OK，
        //    真正的业务状态码由 Result 内部的 code 字段决定。
        return ResponseEntity.ok(result);

    }
    @PostMapping("/api/suppliers")
    public ResponseEntity<Result<Suppliers>> createSuppliers(@RequestBody Suppliers suppliers) {
        //注意供应商暂时没有什么跟进人员啥的
        Suppliers newsupplier= supplierService.createSupplier(suppliers);
        Result<Suppliers> result = Result.success(newsupplier);
        return ResponseEntity.ok(result);
    }
}
