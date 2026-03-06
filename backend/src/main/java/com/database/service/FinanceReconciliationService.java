package com.database.service;

import com.database.mapper.PaymentReconciliationMapper;
import com.database.vo.PaymentReconciliationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务对账服务
 * 提供采购订单的付款、进项发票对账查询
 */
@Service
@RequiredArgsConstructor
public class FinanceReconciliationService {

    private final PaymentReconciliationMapper reconciliationMapper;

    /**
     * 查询采购订单的付款对账信息
     * @param supplierId 供应商ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param purchaseCode 采购订单号（可选）
     * @return 对账信息列表
     */
    public List<PaymentReconciliationVO> getPurchaseReconciliation(
            Long supplierId,
            LocalDate startDate,
            LocalDate endDate,
            String purchaseCode
    ) {
        return reconciliationMapper.selectPurchaseReconciliation(
                supplierId, startDate, endDate, purchaseCode
        );
    }
}
