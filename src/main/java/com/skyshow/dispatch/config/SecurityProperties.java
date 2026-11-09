package com.skyshow.dispatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 账号安全相关配置，绑定 application.yml 中前缀 {@code app.security} 的内容。
 * 当前用于首次启动时自动初始化一个管理员账号。
 */
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    /** 是否在 admin_users 表为空时自动初始化管理员（开发/首次部署用，生产可关闭）。 */
    private boolean initAdminEnabled = true;

    /** 初始管理员登录名。 */
    private String initAdminUsername = "admin";

    /** 初始管理员初始密码（入库前会做 BCrypt 哈希，绝不存明文）。 */
    private String initAdminPassword = "admin123";

    public boolean isInitAdminEnabled() {
        return initAdminEnabled;
    }

    public void setInitAdminEnabled(boolean initAdminEnabled) {
        this.initAdminEnabled = initAdminEnabled;
    }

    public String getInitAdminUsername() {
        return initAdminUsername;
    }

    public void setInitAdminUsername(String initAdminUsername) {
        this.initAdminUsername = initAdminUsername;
    }

    public String getInitAdminPassword() {
        return initAdminPassword;
    }

    public void setInitAdminPassword(String initAdminPassword) {
        this.initAdminPassword = initAdminPassword;
    }
}
