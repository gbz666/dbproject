package com.database.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {

    /** 当前密码 */
    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;

    /** 新密码（建议 6～20 位） */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度为 6～20 位")
    private String newPassword;
}
