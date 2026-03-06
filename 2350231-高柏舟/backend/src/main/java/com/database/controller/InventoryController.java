package com.database.controller;

import com.database.service.InventoryService;
import com.database.vo.InventoryVO;
import com.database.vo.PurchaseInvoiceVO;
import com.database.vo.Result;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @GetMapping("")
    public ResponseEntity<Result<PageInfo<InventoryVO>>>getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCode) {
        PageInfo<InventoryVO> pageInfo = inventoryService.getInventoryByPage(pageNum,pageSize, productCode,productName);
        return ResponseEntity.ok(Result.success(pageInfo));
    }
}