package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class StaffCreateRequest {
    @NotBlank(message = "员工姓名不能为空")
    private String staffName;
    private String staffCode;
    private String email;
    private String phone;
    private String title;
    @NotBlank(message = "初始密码不能为空")
    private String password;
    private List<Integer> roleIds;
}
