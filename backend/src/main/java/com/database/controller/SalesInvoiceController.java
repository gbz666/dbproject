package com.database.controller;

import com.database.dto.SalesInvoiceDTO;
import com.database.dto.SalesInvoiceQuery;
import com.database.service.SalesInvoiceService;
import com.database.vo.Result;
import com.database.vo.SalesInvoiceVO;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 销项发票表 Controller
 */
@RestController
@RequestMapping("/api/sales-invoices")
public class SalesInvoiceController {

    private final SalesInvoiceService invoiceService;

    @Autowired
    public SalesInvoiceController(SalesInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * GET /api/sales-invoices/page: 分页查询销项发票列表
     *
     * @param query 分页及筛选条件
     * @return 200 OK
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<SalesInvoiceVO>>> getPage(SalesInvoiceQuery query) {
        PageInfo<SalesInvoiceVO> data = invoiceService.findPage(query);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * POST /api/sales-invoices: 新增销项发票
     *
     * @param dto            销项发票数据（对应表格一行）
     * @param currentStaffId 当前操作员ID（由拦截器注入）
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<SalesInvoiceVO>> add(
            @RequestBody SalesInvoiceDTO dto,
            @RequestAttribute Long currentStaffId) {
        SalesInvoiceVO vo = invoiceService.createInvoice(dto, currentStaffId);
        Result<SalesInvoiceVO> result = Result.createsuccess(vo);
        result.setMessage("销项发票创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/sales-invoices/{id}: 更新销项发票
     *
     * @param id             发票主键ID
     * @param dto            销项发票数据
     * @param currentStaffId 当前操作员ID（由拦截器注入）
     * @return 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<SalesInvoiceVO>> update(
            @PathVariable Long id,
            @RequestBody SalesInvoiceDTO dto,
            @RequestAttribute Long currentStaffId) {
        SalesInvoiceVO vo = invoiceService.updateInvoice(id, dto, currentStaffId);
        Result<SalesInvoiceVO> result = Result.success(vo);
        result.setMessage("销项发票更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/sales-invoices/{id}: 删除销项发票
     *
     * @param id 发票主键ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}

