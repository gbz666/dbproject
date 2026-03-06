package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "员工姓名不能为空")
    private String staffName;

    @NotBlank(message = "密码不能为空")
    private String password;
}
