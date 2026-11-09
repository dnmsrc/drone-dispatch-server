package com.skyshow.dispatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密组件。
 *
 * <p>只引入 spring-security-crypto 这一个工具模块（不含整套 Security 过滤器链），
 * 提供 BCrypt 哈希能力：每次编码自带随机盐，同一明文每次哈希结果都不同，
 * 校验时用 {@code encoder.matches(明文, 哈希)}。
 */
@Configuration
public class CryptoConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 强度参数默认 10（log rounds），兼顾安全与性能
        return new BCryptPasswordEncoder();
    }
}
