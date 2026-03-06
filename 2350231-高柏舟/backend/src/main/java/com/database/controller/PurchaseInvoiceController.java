package com.database.controller;

import com.database.dto.PurchaseInvoiceDTO;
import com.database.dto.PurchaseInvoiceQuery;
import com.database.service.PurchaseInvoiceService;
import com.database.vo.PurchaseInvoiceVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-invoices")
public class PurchaseInvoiceController {

    @Autowired
    private PurchaseInvoiceService invoiceService;

    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<PurchaseInvoiceVO>>> getPage(PurchaseInvoiceQuery query) {
        PageInfo<PurchaseInvoiceVO> data = invoiceService.findPage(query);
        return ResponseEntity.ok(Result.success(data));
    }

    @PostMapping
    public ResponseEntity<Result<PurchaseInvoiceVO>> add(@RequestBody PurchaseInvoiceDTO dto,
                                                         @RequestAttribute Long currentStaffId) {
        PurchaseInvoiceVO vo = invoiceService.createInvoice(dto, currentStaffId);
        return ResponseEntity.ok(Result.success(vo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<PurchaseInvoiceVO>> update(@PathVariable Long id,
                                                            @RequestBody PurchaseInvoiceDTO dto,
                                                            @RequestAttribute Long currentStaffId) {
        PurchaseInvoiceVO vo = invoiceService.updateInvoice(id, dto, currentStaffId);
        return ResponseEntity.ok(Result.success(vo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(Result.success(null));
    }
}