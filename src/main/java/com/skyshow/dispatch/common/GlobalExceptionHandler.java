package com.skyshow.dispatch.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：所有 controller 抛出的异常都在这被接住，统一装进 Result 信封
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务异常：用异常自己携带的 code 和提示
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    // 兜底：所有没预料到的异常（如数据库连不上），统一 500
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpected(Exception e) {
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}