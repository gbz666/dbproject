package com.database.dto;// package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户创建请求 DTO: 对应前端创建表单，不包含 ID 和 Code 等后端生成的字段。
 * 但包含关联员工的姓名，需要在 Service 层转换为 ID。
 */
@Data
public class CustomerCreateRequest implements Serializable {
    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    private String address;
    private String phone;
    private String email;

    // 员工姓名 (需要 Service 层通过姓名查询 ID)
    private String salesPersonName;
    private String followUpPersonName;
    private String ownerName;

    private Integer paymentTermsDays;
    private String paymentTermsNotes;

    // ... 其他可创建时输入的字段
}