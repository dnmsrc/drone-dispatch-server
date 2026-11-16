package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.PricingRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** pricing_rule 计价规则数据访问：只写 SQL 与行映射，不含业务规则。 */
@Repository
public class PricingRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    public PricingRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 一行查询结果 → 一个 PricingRule；无状态公式，全类复用 */
    private static final RowMapper<PricingRule> MAPPER = (rs, rowNum) -> {
        PricingRule rule = new PricingRule();
        rule.setId(rs.getLong("id"));
        rule.setGroupCode(rs.getString("group_code"));
        rule.setItemKey(rs.getString("item_key"));
        rule.setItemLabel(rs.getString("item_label"));
        rule.setItemValue(rs.getBigDecimal("item_value"));
        rule.setExtra(rs.getString("extra"));
        rule.setSort(rs.getInt("sort"));
        rule.setStatus(rs.getInt("status"));
        rule.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        rule.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return rule;
    };

    /** 明确列出查询列，不用 SELECT * */
    private static final String COLUMNS =
            "id, group_code, item_key, item_label, item_value, extra, sort, status, created_at, updated_at";

    /** 某分组下全部规则（含停用项，管理端视图），按 sort、id 升序 */
    public List<PricingRule> findByGroup(String groupCode) {
        String sql = "SELECT " + COLUMNS + " FROM pricing_rule WHERE group_code = ? ORDER BY sort, id";
        return jdbcTemplate.query(sql, MAPPER, groupCode);
    }

    /** 某分组下仅启用项（用户端 H5 视图），按 sort、id 升序 */
    public List<PricingRule> findEnabledByGroup(String groupCode) {
        String sql = "SELECT " + COLUMNS
                + " FROM pricing_rule WHERE group_code = ? AND status = 1 ORDER BY sort, id";
        return jdbcTemplate.query(sql, MAPPER, groupCode);
    }

    /** 按主键查单条 */
    public Optional<PricingRule> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM pricing_rule WHERE id = ? LIMIT 1";
        List<PricingRule> list = jdbcTemplate.query(sql, MAPPER, id);
        return list.stream().findFirst();
    }

    /** 同分组内 item_key 是否已存在（excludeId 用于编辑排除自身） */
    public boolean existsKey(String groupCode, String itemKey, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM pricing_rule WHERE group_code = ? AND item_key = ?"
                + (excludeId == null ? "" : " AND id <> ?");
        Integer n = excludeId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, groupCode, itemKey)
                : jdbcTemplate.queryForObject(sql, Integer.class, groupCode, itemKey, excludeId);
        return n != null && n > 0;
    }

    /** 该分组规则总数（新增缺省排序号用） */
    public int countByGroup(String groupCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pricing_rule WHERE group_code = ?", Integer.class, groupCode);
        return n == null ? 0 : n;
    }

    /** 新增规则 */
    public int insert(PricingRule rule) {
        String sql = "INSERT INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                rule.getGroupCode(),
                rule.getItemKey(),
                rule.getItemLabel(),
                rule.getItemValue(),
                rule.getExtra(),
                rule.getSort(),
                rule.getStatus());
    }

    /** 按主键改业务字段；updated_at 由数据库 ON UPDATE 刷新 */
    public int update(PricingRule rule) {
        String sql = "UPDATE pricing_rule SET item_key = ?, item_label = ?, item_value = ?, "
                + "extra = ?, sort = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                rule.getItemKey(),
                rule.getItemLabel(),
                rule.getItemValue(),
                rule.getExtra(),
                rule.getSort(),
                rule.getStatus(),
                rule.getId());
    }

    /** 物理删除某分组全部行（整组保存时先清空再重插）；pricing_rule 无外键被引用，删除安全 */
    public int deleteByGroup(String groupCode) {
        return jdbcTemplate.update("DELETE FROM pricing_rule WHERE group_code = ?", groupCode);
    }
}
