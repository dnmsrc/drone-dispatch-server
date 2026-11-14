package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.Venue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.springframework.jdbc.core.RowMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** venues 表数据访问：只写 SQL 与行映射，不含业务规则。 */
@Repository
public class VenueRepository {

    private final JdbcTemplate jdbcTemplate;

    public VenueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 一行查询结果 → 一个 Venue 对象；无状态公式，定义一次全类复用 */
    private static final RowMapper<Venue> VENUE_MAPPER = (rs, rowNum) -> {
        Venue venue = new Venue();
        venue.setId(rs.getLong("id"));
        venue.setName(rs.getString("name"));
        venue.setAddress(rs.getString("address"));
        venue.setLng(rs.getBigDecimal("lng"));
        venue.setLat(rs.getBigDecimal("lat"));
        venue.setEnabled(rs.getInt("enabled"));
        venue.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        venue.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        venue.setDeleted(rs.getInt("deleted"));
        venue.setDeletedAt(rs.getObject("deleted_at", LocalDateTime.class));
        return venue;
    };

    /** 明确列出要查的列，不用 SELECT *（表结构变更时更稳、也不查多余数据） */
    private static final String COLUMNS = "id, name, address, lng, lat, enabled, created_at, updated_at, deleted, deleted_at";

    /** 查询所有「启用且未删除」的场地，按 id 升序 */
    public List<Venue> findEnabledVenues() {
        String sql = "SELECT " + COLUMNS
                + " FROM venues WHERE enabled = 1 AND deleted = 0 ORDER BY id";
        return jdbcTemplate.query(sql, VENUE_MAPPER);
    }

    private static final String INSERT_COLUMNS = "name, address, lng, lat, enabled";

    /** 新增场地 */
    public void insert(Venue venue) {
        String sql = "INSERT INTO venues (" + INSERT_COLUMNS + ") VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                venue.getName(),
                venue.getAddress(),
                venue.getLng(),
                venue.getLat(),
                venue.getEnabled());
    }

    /** 按 id 查询单个「未删除」场地，找不到返回 Optional.empty()（风格对齐 AdminUserRepository） */
    public Optional<Venue> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM venues WHERE id = ? AND deleted = 0 LIMIT 1";
        List<Venue> list = jdbcTemplate.query(sql, VENUE_MAPPER, id);
        return list.stream().findFirst();
    }

    /** 按 id 修改 4 个业务字段；updated_at 由数据库 ON UPDATE 自动刷新，返回受影响行数 */
    public int update(Venue venue) {
        String sql = "UPDATE venues SET name = ?, address = ?, lng = ?, lat = ? "
                + "WHERE id = ? AND deleted = 0";
        return jdbcTemplate.update(sql,
                venue.getName(),
                venue.getAddress(),
                venue.getLng(),
                venue.getLat(),
                venue.getId());
    }

    /** 软删除：把 deleted 置 1 并记录删除时间，不真正 DELETE 数据，返回受影响行数 */
    public int softDeleteById(Long id) {
        String sql = "UPDATE venues SET deleted = 1, deleted_at = NOW(3) "
                + "WHERE id = ? AND deleted = 0";
        return jdbcTemplate.update(sql, id);
    }
}