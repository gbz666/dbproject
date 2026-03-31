package com.database.aop;

import java.lang.annotation.*;

/**
 * 标注在 Controller 方法上，要求当前用户拥有指定角色之一才允许访问。
 * 角色名对应 roles.role_name（如 "admin"、"analyst"）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
}
