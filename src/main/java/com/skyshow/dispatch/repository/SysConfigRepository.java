package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.SysConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * sys_config 数据访问：通用 key-value 读写，不含任何具体配置键的业务语义。
 * config_value 是 JSON 列，这里统一按字符串取出，类型解释交给各业务 Service。
 */
@Repository
public class SysConfigRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SysConfig> MAPPER = (rs, rowNum) -> {
        SysConfig config = new SysConfig();
        config.setId(rs.getLong("id"));
        config.setConfigKey(rs.getString("config_key"));
        // JSON 列用 getString 取原始 JSON 文本（数字取出来即 "15"，数组取出来即 "[...]"）
        config.setConfigValue(rs.getString("config_value"));
        config.setRemark(rs.getString("remark"));
        config.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        config.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return config;
    };

    private static final String COLUMNS = "id, config_key, config_value, remark, created_at, updated_at";

    /** 按键查配置值原始 JSON 文本；不存在返回 empty */
    public Optional<String> findValue(String key) {
        String sql = "SELECT " + COLUMNS + " FROM sys_config WHERE config_key = ? LIMIT 1";
        List<SysConfig> list = jdbcTemplate.query(sql, MAPPER, key);
        return list.stream().findFirst().map(SysConfig::getConfigValue);
    }

    /** 按键 upsert：存在则更新值，不存在则插入（remark 仅插入时给空串） */
    public int upsertValue(String key, String jsonValue) {
        // MySQL 8.4 支持 ON DUPLICATE KEY UPDATE，靠 uk_config_key 命中唯一键
        String sql = "INSERT INTO sys_config (config_key, config_value, remark) VALUES (?, ?, '') "
                + "ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)";
        return jdbcTemplate.update(sql, key, jsonValue);
    }
}
