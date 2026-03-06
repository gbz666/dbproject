package com.database.exception;


// 继承 RuntimeException，让 Spring 事务能够回滚
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
    }

    // 假设业务失败的通用状态码是 400，您可以根据需要调整
    public BusinessException(String message) {
        this(message, 400);
    }

    public int getCode() {
        return code;
    }
}