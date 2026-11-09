package com.skyshow.dispatch.security;

/**
 * 当前登录人的线程级上下文。
 *
 * <p>拦截器在 preHandle 解析 token 后 {@link #set} 写入，Controller/Service 随时 {@link #get} 取用，
 * afterCompletion 必须 {@link #clear} 清理——Tomcat 线程会被池化复用，不清理会导致下一个请求
 * 读到上一个用户的身份（越权风险）。
 */
public final class LoginUserHolder {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private LoginUserHolder() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
