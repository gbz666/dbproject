package com.database.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 客户更新请求 DTO: 用于接收更新请求，所有字段视为可选，null 或缺失表示不修改。
 * 允许空字符串或null，表示清空该字段，具体清空逻辑需在 Service 层实现。
 */
@Data
public class CustomerUpdateRequest implements Serializable {

    private String customerName;

    private String address;
    private String phone;
    private String email;

    // 关联员工姓名 (如果传 null 或空字符串，Service 层需判断是清空还是不修改)
    private String salesPersonName;
    private String followUpPersonName;
    private String ownerName;

    private Integer paymentTermsDays;
    private String paymentTermsNotes;
}