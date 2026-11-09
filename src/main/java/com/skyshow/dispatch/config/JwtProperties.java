package com.skyshow.dispatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项，绑定 application.yml 中前缀 {@code app.jwt} 的内容。
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HS256 签名密钥（至少 32 字节）；生产由环境变量 APP_JWT_SECRET 注入。 */
    private String secret;

    /** token 有效期（分钟）。 */
    private long expireMinutes = 720;

    /** 前端携带 token 的请求头名。 */
    private String header = "Authorization";

    /** token 前缀，含尾部空格，如 {@code "Bearer "}。 */
    private String tokenPrefix = "Bearer ";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
}
