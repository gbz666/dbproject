package com.database.dto;

import lombok.Data; // 确保导入了 Lombok
import java.io.Serializable;
import java.util.Date;

/**
 * 客户详情 DTO: 用于查询响应，必须包含主键ID和业务Code。
 */
@Data
public class CustomerDetailDTO implements Serializable {
    private Long id;
    private String customerCode;

    private String customerName;
    private String address;
    private String phone;
    private String email;

    // 关联员工姓名 (用于前端展示)
    private String salesPersonName;
    private String followUpPersonName;
    private String ownerName;

    private Integer paymentTermsDays;
    private String paymentTermsNotes;

    // 审计字段展示
    private String createdByName;
    private String updatedByName;
    private Date createdAt;
    private Date updatedAt;
}