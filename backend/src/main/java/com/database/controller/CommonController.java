package com.database.controller;

import com.database.service.CommonService;
import com.database.vo.BaseSelectVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/util")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/search/customer")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> getCustomers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        PageInfo<BaseSelectVO> page = commonService.getCustomerPage(pageNum, pageSize, keyword);
        return ResponseEntity.ok(Result.success(page));
    }

    @GetMapping("/search/product")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> searchProduct(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageInfo<BaseSelectVO> list = commonService.getProductPage(pageNum,pageSize,keyword);
        return ResponseEntity.ok(Result.success(list));
    }
    @GetMapping("/search/supplier")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> searchSupplier(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageInfo<BaseSelectVO> list = commonService.getSupplierPage(pageNum,pageSize,keyword);
        return ResponseEntity.ok(Result.success(list));
    }
    @GetMapping("/search/productType")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> searchProductType(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageInfo<BaseSelectVO> list = commonService.getProductType(pageNum,pageSize,keyword);
        return ResponseEntity.ok(Result.success(list));
    }

}