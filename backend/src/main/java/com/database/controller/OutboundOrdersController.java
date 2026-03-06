package com.database.controller;

import com.database.dto.OutboundOrderDTO;
import com.database.service.OutBoundOrdersService;
import com.database.vo.OutboundDetailVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outbound")
public class OutboundOrdersController {

    private final OutBoundOrdersService outboundService;

    @Autowired
    public OutboundOrdersController(OutBoundOrdersService outboundService) {
        this.outboundService = outboundService;
    }

    /**
     * GET /api/outbound/page: 分页查询出库单列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param salesOrderCode 销售订单号（可选）
     * @param customerName 客户名称（可选）
     * @param productName 产品名称（可选）
     * @param serialNumber 序列号（可选）
     * @return 200 OK
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<OutboundDetailVO>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String salesOrderCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String serialNumber) {
        PageInfo<OutboundDetailVO> pageInfo = outboundService.getOutboundByPage(
                pageNum, pageSize, salesOrderCode, customerName, productName, serialNumber);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * POST /api/outbound/create: 创建出库单
     * @param dto 出库单数据
     * @param operatorId 当前操作员ID
     * @return 201 Created
     */
    @PostMapping("/create")
    public ResponseEntity<Result<Void>> create(
            @RequestBody OutboundOrderDTO dto,
            @RequestParam Long operatorId) {
        outboundService.createOutbound(dto, operatorId);
        Result<Void> result = Result.createsuccess(null);
        result.setMessage("创建出库单成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/outbound/update: 修改出库单
     * @param dto 出库单数据
     * @param operatorId 当前操作员ID
     * @return 200 OK
     */
    @PutMapping("/update")
    public ResponseEntity<Result<Void>> update(
            @RequestBody OutboundOrderDTO dto,
            @RequestParam Long operatorId) {
        outboundService.updateOutbound(dto, operatorId);
        Result<Void> result = Result.success("修改出库单成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/outbound/{id}: 删除出库单
     * @param id 出库单主键ID
     * @param operatorId 当前操作员ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long operatorId) {
        outboundService.deleteOutbound(id, operatorId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
