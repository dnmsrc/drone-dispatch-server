package com.skyshow.dispatch.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：所有 controller 抛出的异常都在这被接住，统一装进 Result 信封
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 业务异常：用异常自己携带的 code 和提示（HTTP 状态仍为 200，业务结果看 body.code）
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    // 未认证：方法级 @ResponseStatus 让 HTTP 状态真实返回 401（异常类上的同名注解在被
    // @ExceptionHandler 捕获后不生效，必须标在处理方法上），前端拦截器据此跳登录页
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        return Result.error(401, e.getMessage());
    }

    // 兜底：所有没预料到的异常（如数据库连不上），统一 500；完整堆栈落日志便于排查
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpected(Exception e) {
        log.error("未预期的服务端异常，返回 500", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}
