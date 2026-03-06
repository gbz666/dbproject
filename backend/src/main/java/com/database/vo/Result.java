package com.database.vo;

// src/main/java/.../vo/Result.java (或 common/Result.java)

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一接口返回结果封装类
 * T 表示实际的业务数据类型 (如 Customer, PageInfo<Product> 等)
 */
@Data // Lombok 注解，自动生成 Getter, Setter, toString, equals/hashCode
@NoArgsConstructor // Lombok 注解，生成无参构造函数
public class Result<T> {

    /** 响应状态码：
     * 200: 成功
     * 400: 客户端请求错误 (业务校验失败等)
     * 500: 服务器内部错误/异常
     */
    private Integer code;

    /** 响应消息：对 code 的文字描述 */
    private String message;

    /** 实际返回的业务数据 (可以是单个对象、列表、分页对象等) */
    private T data;

    // --- 构造方法 (私有，通过静态方法创建实例) ---

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // --- 静态工厂方法：成功响应 ---

    /**
     * 成功，无返回数据
     */
    public static Result<Void> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功，带自定义消息，无返回数据
     */
    public static Result<Void> success(String message) {
        return new Result<>(200, message, null);
    }

    /**
     * 成功，带自定义消息，无返回数据（明确返回 Void 类型）
     * 用于避免与 success(T data) 方法重载时的类型推断问题
     */
    @SuppressWarnings("unused")
    public static Result<Void> successMessage(String message) {
        return new Result<>(200, message, null);
    }

    /**
     * 成功，带返回数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    public static <T> Result<T> createsuccess(T data) {
        return new Result<>(201, "创建成功", data);
    }

    /**
     * 成功，带自定义消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // --- 静态工厂方法：失败响应 (业务失败/参数错误等) ---

    /**
     * 业务失败，带默认消息
     */
    public static Result<Void> fail(int code, String message) {
        // 通常使用 400 或业务自定义状态码
        return new Result<>(code, message, null);
    }

    /**
     * 业务失败，带自定义状态码和消息，支持泛型
     */
    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 系统异常/服务器错误 (通常在全局异常处理器中使用)
     */
    public static Result<Void> error() {
        return new Result<>(500, "系统内部错误", null);
    }
}