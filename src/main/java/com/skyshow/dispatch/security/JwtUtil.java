package com.skyshow.dispatch.security;

import com.skyshow.dispatch.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析（jjwt 0.12.x，HS256 对称签名）。
 *
 * <p>令牌内容：subject=管理员 id，附带 username/realName/role，带签发时间与过期时间。
 * 解析失败（过期、篡改、格式错）会抛 jjwt 的 JwtException，由拦截器统一转成 401。
 */
@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        // HS256 要求密钥至少 256 bit（32 字节）
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 为指定登录人生成 token。 */
    public String generateToken(LoginUser user) {
        long now = System.currentTimeMillis();
        long expiration = now + properties.getExpireMinutes() * 60_000L;
        return Jwts.builder()
                .subject(String.valueOf(user.getAdminId()))
                .claim("username", user.getUsername())
                .claim("realName", user.getRealName())
                .claim("role", user.getRole())
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(signingKey)
                .compact();
    }

    /** 校验并解析 token，失败抛 JwtException。 */
    public LoginUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new LoginUser(
                Long.valueOf(claims.getSubject()),
                claims.get("username", String.class),
                claims.get("realName", String.class),
                claims.get("role", String.class)
        );
    }
}
