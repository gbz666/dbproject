package com.database.controller;

import com.database.service.FinanceReconciliationService;
import com.database.vo.PaymentReconciliationVO;
import com.database.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务对账接口：采购订单付款、进项发票对账查询
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceReconciliationController {

    private final FinanceReconciliationService financeReconciliationService;

    @Autowired
    public FinanceReconciliationController(FinanceReconciliationService financeReconciliationService) {
        this.financeReconciliationService = financeReconciliationService;
    }

    /**
     * GET /api/finance/purchase-reconciliation: 查询采购订单付款对账信息
     * @param supplierId 供应商ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param purchaseCode 采购订单号（可选）
     * @return 200 OK
     */
    @GetMapping("/purchase-reconciliation")
    public ResponseEntity<Result<List<PaymentReconciliationVO>>> getPurchaseReconciliation(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String purchaseCode) {
        List<PaymentReconciliationVO> data = financeReconciliationService.getPurchaseReconciliation(
                supplierId, startDate, endDate, purchaseCode);
        return ResponseEntity.ok(Result.success(data));
    }
}
