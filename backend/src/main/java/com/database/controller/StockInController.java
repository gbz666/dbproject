package com.database.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.database.dto.StockInDTO;
import com.database.service.StockInService;
import com.database.vo.Result;
import com.database.vo.StockInVO;
import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-in")
public class StockInController {

    private final StockInService stockInService;

    public StockInController(StockInService stockInService) {
        this.stockInService = stockInService;
    }

    /**
     * 创建入库单
     * @param dto 入库信息
     * @param operatorId 操作人ID (staffs.id)
     */
    @PostMapping("/create")
    public ResponseEntity<Result<String>> create(@RequestBody StockInDTO dto, @RequestParam Long operatorId) {
        stockInService.createStockIn(dto, operatorId);
        return ResponseEntity.ok(Result.success("创建入库单成功"));
    }

    /**
     * 修改入库单
     * @param dto 修改信息
     * @param operatorId 操作人ID
     */
    @PutMapping("/update")
    public ResponseEntity<Result<String>> update(@RequestBody StockInDTO dto, @RequestParam Long operatorId) {
        stockInService.updateStockIn(dto, operatorId);
        return ResponseEntity.ok(Result.success("修改入库单成功"));
    }

    /**
     * 分页查询入库单列表
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<StockInVO>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String purchaseOrderCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String serialNumber
    ) {
        PageInfo<StockInVO> pageInfo = stockInService.getStockInByPage(
                pageNum, pageSize, purchaseOrderCode, supplierName, productName,serialNumber);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * 删除入库单 (软删除)
     * @param id 入库单主键ID
     * @param operatorId 操作人ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> delete(@PathVariable Long id, @RequestParam Long operatorId) {
        stockInService.deleteStockIn(id, operatorId);
        return ResponseEntity.ok(Result.success("删除入库单成功"));
    }
}