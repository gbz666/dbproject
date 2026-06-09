package com.database.controller;

import com.database.dto.PaymentReceiptDTO;
import com.database.dto.PaymentReceiptQuery;
import com.database.service.PaymentReceiptService;
import com.database.vo.PaymentReceiptVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-receipts")
public class PaymentReceiptController {

    @Autowired
    private PaymentReceiptService service;

    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<PaymentReceiptVO>>> getPage(PaymentReceiptQuery query) {
        PageInfo<PaymentReceiptVO> data = service.findPage(query);
        return ResponseEntity.ok(Result.success(data));
    }

    @PostMapping
    public ResponseEntity<Result<Void>> create(
            @RequestBody PaymentReceiptDTO dto,
            @RequestAttribute Long currentStaffId) {
        service.create(dto, currentStaffId);
        return ResponseEntity.ok(Result.success("创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> update(
            @PathVariable Long id,
            @RequestBody PaymentReceiptDTO dto,
            @RequestAttribute Long currentStaffId) {
        service.update(id, dto, currentStaffId);
        return ResponseEntity.ok(Result.success("更新成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Result.success("删除成功"));
    }
}
