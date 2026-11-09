package com.skyshow.dispatch.service;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.common.UnauthorizedException;
import com.skyshow.dispatch.config.JwtProperties;
import com.skyshow.dispatch.dto.AdminUserInfo;
import com.skyshow.dispatch.dto.LoginRequest;
import com.skyshow.dispatch.dto.LoginResponse;
import com.skyshow.dispatch.entity.AdminUser;
import com.skyshow.dispatch.repository.AdminUserRepository;
import com.skyshow.dispatch.security.JwtUtil;
import com.skyshow.dispatch.security.LoginUser;
import com.skyshow.dispatch.security.LoginUserHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理端登录鉴权服务：校验账号密码（BCrypt），签发 JWT。
 */
@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AuthService(AdminUserRepository adminUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       JwtProperties jwtProperties) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || !hasText(request.username()) || !hasText(request.password())) {
            throw new BizException(400, "用户名和密码不能为空");
        }

        // 账号不存在与密码错误返回相同提示，避免被枚举用户名
        AdminUser user = adminUserRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new BizException(401, "用户名或密码错误"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }

        if (user.getEnabled() == null || user.getEnabled() != 1) {
            throw new BizException(403, "账号已停用，请联系管理员");
        }

        adminUserRepository.updateLastLoginAt(user.getId());

        LoginUser loginUser = new LoginUser(
                user.getId(), user.getUsername(), user.getRealName(), user.getRole());
        String token = jwtUtil.generateToken(loginUser);
        AdminUserInfo info = new AdminUserInfo(
                user.getId(), user.getUsername(), user.getRealName(), user.getRole());
        return new LoginResponse(token, "Bearer", jwtProperties.getExpireMinutes(), info);
    }

    /** 从请求期上下文取当前登录人（由拦截器保证已填充）。 */
    public AdminUserInfo currentUser() {
        LoginUser user = LoginUserHolder.get();
        if (user == null) {
            throw new UnauthorizedException("未登录或登录已失效，请重新登录");
        }
        return new AdminUserInfo(
                user.getAdminId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
