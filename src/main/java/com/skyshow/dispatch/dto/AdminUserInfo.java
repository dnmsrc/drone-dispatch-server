package com.skyshow.dispatch.dto;

/**
 * 返回给前端的当前登录人信息（不含密码哈希等敏感字段）。
 */
public record AdminUserInfo(Long id, String username, String realName, String role) {
}
