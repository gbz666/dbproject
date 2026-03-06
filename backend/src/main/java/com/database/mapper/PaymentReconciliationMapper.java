package com.database.mapper;

import com.database.vo.PaymentReconciliationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 付款对账 Mapper
 * 用于查询采购订单的付款、进项发票对账信息
 */
@Mapper
public interface PaymentReconciliationMapper {

    /**
     * 查询采购订单的付款对账信息
     * @param supplierId 供应商ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param purchaseCode 采购订单号（可选）
     * @return 对账信息列表
     */
    List<PaymentReconciliationVO> selectPurchaseReconciliation(
            @Param("supplierId") Long supplierId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("purchaseCode") String purchaseCode
    );
}
