package com.skyshow.dispatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域（CORS）配置项，绑定 application.yml 中前缀 {@code app.cors} 的内容。
 *
 * <p>把允许来源、方法、请求头等做成可配置项，开发 / 生产环境各自给白名单，
 * 代码里不写死任何域名；新增可信前端来源时只改 yml，不用改 Java 代码。
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /** 允许的来源（origin）白名单，如 {@code http://localhost:8091}；支持模式通配。 */
    private List<String> allowedOrigins = new ArrayList<>();

    /** 允许的 HTTP 方法；默认覆盖常用 REST 动词，OPTIONS 为预检请求必须放行。 */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    /** 允许携带的请求头；{@code *} 放通，含后续登录用的 token 等自定义头。 */
    private List<String> allowedHeaders = List.of("*");

    /** 允许浏览器读取的自定义响应头（默认只能读基础响应头，自定义头需在此暴露）。 */
    private List<String> exposedHeaders = List.of(
            "Authorization",
            "Content-Disposition",
            "X-Total-Count",
            "X-Request-Id"
    );

    /** 是否允许携带凭证（Cookie、Authorization 等）；登录态场景需要开启。 */
    private boolean allowCredentials = true;

    /** 预检结果缓存秒数：有效期内浏览器对同一路由不重复发送 OPTIONS 预检。 */
    private long maxAge = 3600;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
