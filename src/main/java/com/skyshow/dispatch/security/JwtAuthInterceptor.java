package com.skyshow.dispatch.security;

import com.skyshow.dispatch.common.UnauthorizedException;
import com.skyshow.dispatch.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器：保护需要登录的 /api 接口。
 *
 * <p>处理顺序：CorsFilter（最高优先级过滤器）先处理跨域与 OPTIONS 预检，
 * 请求进入 DispatcherServlet 后才轮到本拦截器，因此这里对 OPTIONS 直接放行做双保险。
 * 其余请求必须携带形如 {@code Authorization: Bearer <token>} 的有效令牌，
 * 解析出的 {@link LoginUser} 存入 {@link LoginUserHolder} 供业务层使用。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public JwtAuthInterceptor(JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求不带 token，直接放行（跨域头由 CorsFilter 负责）
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(jwtProperties.getHeader());
        String prefix = jwtProperties.getTokenPrefix();
        if (header == null || !header.startsWith(prefix)) {
            throw new UnauthorizedException("未登录或登录已失效，请重新登录");
        }

        String token = header.substring(prefix.length()).trim();
        try {
            LoginUser user = jwtUtil.parse(token);
            LoginUserHolder.set(user);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 过期、签名错误、格式非法等，统一按未认证处理
            throw new UnauthorizedException("登录凭证无效或已过期，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理：Tomcat 线程池化复用，残留会导致下个请求串号
        LoginUserHolder.clear();
    }
}
