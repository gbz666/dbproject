package com.database.controller;

import com.database.dto.StockInDTO;
import com.database.service.StockInService;
import com.database.vo.Result;
import com.database.vo.StockInVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-in")
public class StockInController {

    private final StockInService stockInService;

    @Autowired
    public StockInController(StockInService stockInService) {
        this.stockInService = stockInService;
    }

    /**
     * GET /api/stock-in/page: 分页查询入库单列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param purchaseOrderCode 采购订单号（可选）
     * @param supplierName 供应商名称（可选）
     * @param productName 产品名称（可选）
     * @param serialNumber 序列号（可选）
     * @return 200 OK
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<StockInVO>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String purchaseOrderCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String serialNumber) {
        PageInfo<StockInVO> pageInfo = stockInService.getStockInByPage(
                pageNum, pageSize, purchaseOrderCode, supplierName, productName, serialNumber);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * POST /api/stock-in/create: 创建入库单
     * @param dto 入库单数据
     * @param operatorId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping("/create")
    public ResponseEntity<Result<Void>> create(
            @RequestBody StockInDTO dto,
            @RequestParam Long operatorId) {
        stockInService.createStockIn(dto, operatorId);
        Result<Void> result = Result.createsuccess(null);
        result.setMessage("创建入库单成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/stock-in/update: 修改入库单
     * @param dto 入库单数据
     * @param operatorId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/update")
    public ResponseEntity<Result<Void>> update(
            @RequestBody StockInDTO dto,
            @RequestParam Long operatorId) {
        stockInService.updateStockIn(dto, operatorId);
        Result<Void> result = Result.success("修改入库单成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/stock-in/{id}: 软删除入库单
     * @param id 入库单主键ID
     * @param operatorId 当前操作员ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long operatorId) {
        stockInService.deleteStockIn(id, operatorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
