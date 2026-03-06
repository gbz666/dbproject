package com.database.controller;

import com.database.service.InventoryService;
import com.database.vo.InventoryVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * GET /api/inventory: 分页查询库存列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param productCode 产品编码（可选）
     * @param productName 产品名称（可选）
     * @return 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<PageInfo<InventoryVO>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName) {
        PageInfo<InventoryVO> pageInfo = inventoryService.getInventoryByPage(
                pageNum, pageSize, productCode, productName);
        return ResponseEntity.ok(Result.success(pageInfo));
    }
}
