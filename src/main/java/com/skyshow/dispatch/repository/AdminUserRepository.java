package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.AdminUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * admin_users 数据访问。当前阶段直接用 JdbcTemplate（与 HealthService 一致），
 * 后续若引入 MyBatis/JPA 再平滑替换，service 层不感知。
 */
@Repository
public class AdminUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AdminUser> MAPPER = (rs, rowNum) -> {
        AdminUser u = new AdminUser();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRealName(rs.getString("real_name"));
        u.setRole(rs.getString("role"));
        u.setEnabled(rs.getInt("enabled"));
        u.setLastLoginAt(rs.getObject("last_login_at", LocalDateTime.class));
        u.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        u.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return u;
    };

    private static final String COLUMNS =
            "id, username, password_hash, real_name, role, enabled, last_login_at, created_at, updated_at";

    public long count() {
        Long n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin_users", Long.class);
        return n == null ? 0L : n;
    }

    public Optional<AdminUser> findByUsername(String username) {
        List<AdminUser> list = jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM admin_users WHERE username = ? LIMIT 1",
                MAPPER, username);
        return list.stream().findFirst();
    }

    /** 新建账号（默认启用），返回受影响行数。 */
    public int insert(AdminUser user) {
        return jdbcTemplate.update(
                "INSERT INTO admin_users (username, password_hash, real_name, role, enabled) "
                        + "VALUES (?, ?, ?, ?, 1)",
                user.getUsername(), user.getPasswordHash(), user.getRealName(), user.getRole());
    }

    public int updateLastLoginAt(Long id) {
        return jdbcTemplate.update("UPDATE admin_users SET last_login_at = NOW(3) WHERE id = ?", id);
    }
}
