package com.skyshow.dispatch.dto;

/**
 * 登录成功响应：JWT 令牌 + 有效期 + 当前用户信息。
 *
 * @param token            JWT 字符串
 * @param tokenType        令牌类型，固定 Bearer
 * @param expiresInMinutes 有效期（分钟），与配置一致，便于前端安排续期/跳转
 * @param user             当前登录人
 */
public record LoginResponse(String token, String tokenType, long expiresInMinutes, AdminUserInfo user) {
}
