package com.database.controller;

import com.database.dto.PaymentExpenseDTO;
import com.database.dto.PaymentExpenseQuery;
import com.database.service.PaymentExpenseService;
import com.database.vo.PaymentExpenseVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment-expenses")
public class PaymentExpenseController {

    @Autowired
    private PaymentExpenseService service;

    @GetMapping("/page")
    public ResponseEntity<Result<PageInfo<PaymentExpenseVO>>> getPage(PaymentExpenseQuery query) {
        PageInfo<PaymentExpenseVO> data = service.findPage(query);
        return ResponseEntity.ok(Result.success(data));
    }

    @PostMapping
    public ResponseEntity<Result<Void>> create(
            @Valid @RequestBody PaymentExpenseDTO dto,
            @RequestAttribute Long currentStaffId) {
        service.create(dto, currentStaffId);
        return ResponseEntity.ok(Result.success("创建成功"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentExpenseDTO dto,
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
