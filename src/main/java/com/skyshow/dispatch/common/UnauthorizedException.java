package com.skyshow.dispatch.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 未认证 / 登录失效异常：请求缺少有效 token，或 token 非法、过期。
 *
 * <p>{@link ResponseStatus} 让 HTTP 状态码为 401，同时仍由全局异常处理包进统一 Result 信封，
 * 前端据此跳转登录页。
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
