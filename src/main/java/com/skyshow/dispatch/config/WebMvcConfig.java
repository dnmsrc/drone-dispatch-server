package com.skyshow.dispatch.config;

import com.skyshow.dispatch.security.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 JWT 拦截器：默认拦截所有 /api 接口，白名单除外。
 *
 * <p>注意与 {@link CorsWebConfig} 的分工：CorsFilter 是 Servlet 过滤器、最高优先级，
 * 在拦截器之前处理跨域与 OPTIONS 预检；本类只负责登录态校验。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    public WebMvcConfig(JwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 登录接口必须匿名可访问，否则拿不到 token
                        "/api/auth/login",
                        // 健康检查，供探活/监控使用
                        "/api/health",
                        // 演示异常接口，保持可直接访问
                        "/api/demo/error",
                        // H5 用户端只读：下单计价规则与退款阶梯，匿名可浏览（管理端写接口仍需 ADMIN）
                        "/api/pricing/public",
                        "/api/pricing/refund-rules",
                        // H5 用户端只读：档期日历（可预约时段），匿名可浏览
                        "/api/schedule/public"
                );
    }
}
