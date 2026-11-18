package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.ScheduleDay;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** schedule_days 数据访问：只写 SQL 与行映射，不含档期业务规则。 */
@Repository
public class ScheduleDayRepository {

    private final JdbcTemplate jdbcTemplate;

    public ScheduleDayRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ScheduleDay> MAPPER = (rs, rowNum) -> {
        ScheduleDay day = new ScheduleDay();
        day.setId(rs.getLong("id"));
        day.setScheduleDate(rs.getObject("schedule_date", LocalDate.class));
        day.setClosed(rs.getInt("closed"));
        day.setSlots(rs.getString("slots"));
        day.setNote(rs.getString("note"));
        day.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        day.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return day;
    };

    private static final String COLUMNS =
            "id, schedule_date, closed, slots, note, created_at, updated_at";

    /** 查 [from, to] 区间内有显式配置的日期（未配置的日期不在结果里，由 Service 回退默认） */
    public List<ScheduleDay> findBetween(LocalDate from, LocalDate to) {
        String sql = "SELECT " + COLUMNS + " FROM schedule_days "
                + "WHERE schedule_date BETWEEN ? AND ? ORDER BY schedule_date";
        return jdbcTemplate.query(sql, MAPPER, from, to);
    }

    /** 查单日配置，不存在返回 empty（表示该日沿用全局默认） */
    public Optional<ScheduleDay> findByDate(LocalDate date) {
        String sql = "SELECT " + COLUMNS + " FROM schedule_days WHERE schedule_date = ? LIMIT 1";
        List<ScheduleDay> list = jdbcTemplate.query(sql, MAPPER, date);
        return list.stream().findFirst();
    }

    /** 按日期 upsert 单日配置（slots 传 JSON 文本或 null），靠 uk_schedule_date 命中 */
    public int upsertDay(LocalDate date, int closed, String slotsJson, String note) {
        String sql = "INSERT INTO schedule_days (schedule_date, closed, slots, note) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE closed = VALUES(closed), slots = VALUES(slots), note = VALUES(note)";
        return jdbcTemplate.update(sql, date, closed, slotsJson, note);
    }

    /** 删除单日配置行（恢复为沿用全局默认） */
    public int deleteByDate(LocalDate date) {
        return jdbcTemplate.update("DELETE FROM schedule_days WHERE schedule_date = ?", date);
    }

    /**
     * 统计某日某时段的有效订单数（用于 openSlots 过滤已订满时段）。
     * 有效 = 未软删，且状态不属于已取消 / 已退款（这些状态不占用档期名额）。
     */
    public int countBookedSlot(LocalDate date, String timeSlot) {
        String sql = "SELECT COUNT(*) FROM orders "
                + "WHERE perform_date = ? AND time_slot = ? AND deleted = 0 "
                + "AND status NOT IN ('CANCELLED', 'REFUNDED')";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, date, timeSlot);
        return n == null ? 0 : n;
    }

    /**
     * 一次聚合统计区间内「每个日期 × 每个时段」的有效订单数。
     * 替代逐日逐段调用 {@link #countBookedSlot}（默认 3 时段 31 天 = 93 条 SQL）。
     * 走 idx_orders_perform，口径与 countBookedSlot 完全一致。
     */
    public List<BookedSlotCount> listBookedCounts(LocalDate from, LocalDate to) {
        String sql = "SELECT perform_date, time_slot, COUNT(*) AS cnt FROM orders "
                + "WHERE perform_date BETWEEN ? AND ? AND deleted = 0 "
                + "AND status NOT IN ('CANCELLED', 'REFUNDED') "
                + "GROUP BY perform_date, time_slot ORDER BY perform_date, time_slot";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new BookedSlotCount(
                        rs.getObject("perform_date", LocalDate.class),
                        rs.getString("time_slot"),
                        rs.getInt("cnt")),
                from, to);
    }

    /** 统计某日有效订单总数（设休整日前的拦截判定用，口径同上） */
    public int countBookedDay(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM orders "
                + "WHERE perform_date = ? AND deleted = 0 "
                + "AND status NOT IN ('CANCELLED', 'REFUNDED')";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, date);
        return n == null ? 0 : n;
    }

    /** 聚合结果行：某日某时段的有效订单数 */
    public record BookedSlotCount(LocalDate date, String slot, int count) {
    }
}
