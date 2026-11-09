package com.skyshow.dispatch.security;

/**
 * 登录成功后写入 token、并在请求期内通过 {@link LoginUserHolder} 透传的当前登录人。
 * 字段全部 final，构造后不可变。
 */
public class LoginUser {

    private final Long adminId;
    private final String username;
    private final String realName;
    private final String role;

    public LoginUser(Long adminId, String username, String realName, String role) {
        this.adminId = adminId;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getRole() {
        return role;
    }
}
