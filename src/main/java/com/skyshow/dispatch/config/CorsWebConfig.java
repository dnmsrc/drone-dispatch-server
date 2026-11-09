package com.skyshow.dispatch.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Web 层跨域（CORS）配置。
 *
 * <p><b>为什么用最高优先级的 {@link CorsFilter}，而不是 {@code @CrossOrigin} 或
 * {@code WebMvcConfigurer}：</b>后续会接入登录鉴权拦截器（JWT）。浏览器对非简单请求
 * 会先发送一个不带 token 的 OPTIONS 预检请求，若鉴权逻辑先执行，预检会被 401 拦下，
 * 真正的业务请求根本发不出去。把 CorsFilter 注册为过滤器链最高优先级
 * （{@link Ordered#HIGHEST_PRECEDENCE}），可保证预检在任何鉴权 / 业务逻辑之前被处理。
 *
 * <p>当前后端所有接口都在 {@code /api} 前缀下，过滤器也只对该前缀生效。
 */
@Configuration
public class CorsWebConfig {

    /** 后端统一接口前缀，CORS 过滤器只拦截这部分请求。 */
    private static final String API_PATH = "/api/*";

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        // 允许携带凭证（allowCredentials=true）时禁止使用 allowedOrigins("*")，
        // 改用 allowedOriginPatterns 配合精确来源白名单，既安全又支持带 Cookie / token。
        config.setAllowedOriginPatterns(props.getAllowedOrigins());
        config.setAllowedMethods(props.getAllowedMethods());
        config.setAllowedHeaders(props.getAllowedHeaders());
        config.setExposedHeaders(props.getExposedHeaders());
        config.setAllowCredentials(props.isAllowCredentials());
        config.setMaxAge(props.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API_PATH, config);

        FilterRegistrationBean<CorsFilter> registration =
                new FilterRegistrationBean<>(new CorsFilter(source));
        // 最高优先级：确保 OPTIONS 预检早于一切鉴权过滤器 / 拦截器执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns(API_PATH);
        registration.setName("corsFilter");
        return registration;
    }
}
