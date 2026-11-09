package com.skyshow.dispatch.dto;

/**
 * 登录请求体：{@code {"username":"admin","password":"..."}}。
 * 用 record 表达不可变 DTO，Jackson 可直接反序列化。
 */
public record LoginRequest(String username, String password) {
}
