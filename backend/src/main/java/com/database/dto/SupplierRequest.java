package com.database.dto;

import lombok.Data;

/**
 * 供应商创建/更新请求 DTO
 */
@Data
public class SupplierRequest {
    // 基础信息
    private String supplierName; // 供应商名称
    private String shortName;    // 简称
    private String mainBusiness; // 主营业务描述
    private String taxNo;        // 税号
    private String address;      // 地址
    private String phone;        // 电话
    private String email;        // 邮箱

    // 关联员工姓名（Service层将它们转换为ID）
    private String salesPersonName;     // 负责销售/关系维护的员工姓名
    private String followUpPersonName;  // 跟进人员工姓名
    private String ownerName;           // 供应商归属人姓名
}