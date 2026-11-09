package com.skyshow.dispatch.config;

import com.skyshow.dispatch.entity.AdminUser;
import com.skyshow.dispatch.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动初始化：当 admin_users 为空且开关打开时，创建一个默认管理员，
 * 保证系统装好后即可登录。已有账号时不做任何处理（不覆盖、不重置密码）。
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public DataInitializer(AdminUserRepository adminUserRepository,
                           PasswordEncoder passwordEncoder,
                           SecurityProperties securityProperties) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAdminUser() {
        if (!securityProperties.isInitAdminEnabled()) {
            return;
        }
        if (adminUserRepository.count() > 0) {
            return;
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(securityProperties.getInitAdminUsername());
        // 明文密码先做 BCrypt 哈希再入库，表里绝不存明文
        admin.setPasswordHash(passwordEncoder.encode(securityProperties.getInitAdminPassword()));
        admin.setRealName("系统管理员");
        admin.setRole("ADMIN");
        admin.setEnabled(1);
        adminUserRepository.insert(admin);

        log.info("Initial admin created: username={}, role=ADMIN. Please change the default password ASAP.",
                securityProperties.getInitAdminUsername());
    }
}
