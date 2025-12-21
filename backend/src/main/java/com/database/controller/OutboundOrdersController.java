package com.database.controller;

import com.database.dto.OutboundOrderDTO;
import com.database.service.OutBoundOrdersService;
import com.database.vo.OutboundDetailVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/outbound")
public class OutboundOrdersController {

    private final OutBoundOrdersService outboundService;

    OutboundOrdersController(OutBoundOrdersService outboundService) {
        this.outboundService = outboundService;
    }

    /**
     * 创建出库单 - 统一 ResponseEntity 包装
     */
    @PostMapping("/create")
    public ResponseEntity<Result<String>> create(@RequestBody OutboundOrderDTO dto, @RequestParam Long operatorId) {
        outboundService.createOutbound(dto, operatorId);
        return ResponseEntity.ok(Result.success("创建出库单成功"));
    }

    /**
     * 修改出库单 - 统一 ResponseEntity 包装
     */
    @PutMapping("/update")
    public ResponseEntity<Result<String>> update(@RequestBody OutboundOrderDTO dto, @RequestParam Long operatorId) {
        outboundService.updateOutbound(dto, operatorId);
        return ResponseEntity.ok(Result.success("修改出库单成功"));
    }

    /**
     * 分页查询出库单 - 统一 ResponseEntity 包装
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<OutboundDetailVO>>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String salesOrderCode,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String serialNumber
    ) {
        PageInfo<OutboundDetailVO> pageInfo = outboundService.getOutboundByPage(
                pageNum, pageSize, salesOrderCode, customerName, productName, serialNumber);
        return ResponseEntity.ok(Result.success(pageInfo));
    }

    /**
     * 删除出库单 - 统一 ResponseEntity 包装
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<String>> delete(@PathVariable Long id, @RequestParam Long operatorId) {
        outboundService.deleteOutbound(id, operatorId);
        return ResponseEntity.ok(Result.success("删除成功"));
    }
}