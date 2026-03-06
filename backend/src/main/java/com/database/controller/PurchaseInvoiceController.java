package com.database.controller;

import com.database.dto.PurchaseInvoiceDTO;
import com.database.dto.PurchaseInvoiceQuery;
import com.database.service.PurchaseInvoiceService;
import com.database.vo.PurchaseInvoiceVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-invoices")
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService invoiceService;

    @Autowired
    public PurchaseInvoiceController(PurchaseInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * GET /api/purchase-invoices/page: 分页查询进项发票列表
     * @param query 分页及筛选条件
     * @return 200 OK
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<PurchaseInvoiceVO>>> getPage(PurchaseInvoiceQuery query) {
        PageInfo<PurchaseInvoiceVO> data = invoiceService.findPage(query);
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * GET /api/purchase-invoices/{id}: 按ID查询进项发票（含完整明细，用于编辑）
     * @param id 发票主键ID
     * @return 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<PurchaseInvoiceVO>> getById(@PathVariable Long id) {
        PurchaseInvoiceVO vo = invoiceService.getById(id);
        if (vo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Result.fail(404, "未找到该进项发票", (PurchaseInvoiceVO) null));
        }
        return ResponseEntity.ok(Result.success(vo));
    }

    /**
     * POST /api/purchase-invoices: 新增进项发票
     * @param dto 进项发票数据
     * @param currentStaffId 当前操作员ID（由拦截器注入）
     * @return 201 Created
     */
    @PostMapping
    public ResponseEntity<Result<PurchaseInvoiceVO>> add(
            @RequestBody PurchaseInvoiceDTO dto,
            @RequestAttribute Long currentStaffId) {
        PurchaseInvoiceVO vo = invoiceService.createInvoice(dto, currentStaffId);
        Result<PurchaseInvoiceVO> result = Result.createsuccess(vo);
        result.setMessage("进项发票创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/purchase-invoices/{id}: 更新进项发票
     * @param id 发票主键ID
     * @param dto 进项发票数据
     * @param currentStaffId 当前操作员ID（由拦截器注入）
     * @return 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<PurchaseInvoiceVO>> update(
            @PathVariable Long id,
            @RequestBody PurchaseInvoiceDTO dto,
            @RequestAttribute Long currentStaffId) {
        PurchaseInvoiceVO vo = invoiceService.updateInvoice(id, dto, currentStaffId);
        Result<PurchaseInvoiceVO> result = Result.success(vo);
        result.setMessage("进项发票更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/purchase-invoices/{id}: 删除进项发票
     * @param id 发票主键ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
