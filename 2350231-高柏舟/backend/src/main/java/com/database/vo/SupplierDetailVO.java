package com.database.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商详情返回 DTO (包含关联员工姓名)
 */
@Data
public class SupplierDetailVO {
    private Long id;
    private String supplierCode; // 业务编号
    private String supplierName;
    private String shortName;
    private String mainBusiness;
    private String taxNo;
    private String address;
    private String phone;
    private String email;

    // 关联员工姓名 (从 staffs 表 JOIN 得到)
    private String salesPersonName;
    private String followUpPersonName;
    private String ownerName;

    // 审计字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByStaffName; // 创建人姓名
    private String updatedByStaffName; // 最后修改人姓名
}