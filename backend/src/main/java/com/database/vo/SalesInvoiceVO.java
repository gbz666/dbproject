package com.database.vo;

import com.database.dto.SalesInvoiceDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销项发票 VO（继承 DTO，增加统计展示字段）
 */
@Data
public class SalesInvoiceVO extends SalesInvoiceDTO {

    /**
     * 销项发票主键ID
     */
    private Long id;

    /**
     * 平均开票时间（单位：天）
     * 例如：订单日期到各次开票日期的平均间隔
     */
    private Double avgInvoiceDays;

    /**
     * 未开金额 = 订单总额 - 已开含税总额
     */
    private BigDecimal pendingInvoiceAmount;
}

