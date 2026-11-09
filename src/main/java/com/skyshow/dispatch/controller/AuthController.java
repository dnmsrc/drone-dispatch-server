package com.skyshow.dispatch.controller;

import com.skyshow.dispatch.common.Result;
import com.skyshow.dispatch.dto.AdminUserInfo;
import com.skyshow.dispatch.dto.LoginRequest;
import com.skyshow.dispatch.dto.LoginResponse;
import com.skyshow.dispatch.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鉴权接口。登录在拦截器白名单内可匿名访问；/me 需携带有效 token。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 账号密码登录，成功返回 JWT。 */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /** 查询当前登录人信息（前端刷新页面后恢复登录态用）。 */
    @GetMapping("/me")
    public Result<AdminUserInfo> me() {
        return Result.success(authService.currentUser());
    }
}
