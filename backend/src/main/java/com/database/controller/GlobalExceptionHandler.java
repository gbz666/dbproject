package com.database.controller;

import com.database.exception.BusinessException;
import com.database.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 统一处理所有 Controller 抛出的异常
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        // 1. 记录日志
        // log.error("业务异常：{}", e.getMessage(), e); // 假设您有日志工具

        // 2. 构造失败的 Result 对象
        Result<?> result = Result.fail(e.getCode(), e.getMessage());

        // 3. 返回 ResponseEntity，HTTP 状态码使用 400 Bad Request 或 404 Not Found
        //    （取决于您在 BusinessException 中设置的 code），但这里统一返回 400 更常见。
        HttpStatus httpStatus = (e.getCode() >= 400 && e.getCode() < 500) ?
                HttpStatus.valueOf(e.getCode()) :
                HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(result, httpStatus);
    }

    /**
     * 兜底处理其他未捕获的运行时异常
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGeneralException(Exception e) {
        // log.error("系统内部错误：{}", e.getMessage(), e);

        // 返回 500 Internal Server Error
        Result<?> result = Result.fail(500, "系统繁忙，请稍后再试: " + e.getMessage());
        log.error("Unhandled Exception caught in GlobalExceptionHandler:", e);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}