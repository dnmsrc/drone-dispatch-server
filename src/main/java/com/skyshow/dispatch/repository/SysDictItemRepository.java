package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.SysDictItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** sys_dict_item 字典项数据访问：只写 SQL 与行映射，不含业务规则。 */
@Repository
public class SysDictItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysDictItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 一行查询结果 → 一个 SysDictItem 对象；无状态公式，全类复用 */
    private static final RowMapper<SysDictItem> MAPPER = (rs, rowNum) -> {
        SysDictItem item = new SysDictItem();
        item.setId(rs.getLong("id"));
        item.setDictCode(rs.getString("dict_code"));
        item.setItemKey(rs.getString("item_key"));
        item.setItemLabel(rs.getString("item_label"));
        item.setItemValue(rs.getBigDecimal("item_value"));
        item.setExtra(rs.getString("extra"));
        item.setSort(rs.getInt("sort"));
        item.setStatus(rs.getInt("status"));
        item.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        item.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return item;
    };

    /** 明确列出查询列，不用 SELECT * */
    private static final String COLUMNS =
            "id, dict_code, item_key, item_label, item_value, extra, sort, status, created_at, updated_at";

    /** 某类型下全部字典项，按 sort 升序、id 次序兜底 */
    public List<SysDictItem> findByDictCode(String dictCode) {
        String sql = "SELECT " + COLUMNS + " FROM sys_dict_item WHERE dict_code = ? ORDER BY sort, id";
        return jdbcTemplate.query(sql, MAPPER, dictCode);
    }

    /** 按主键查单个字典项 */
    public Optional<SysDictItem> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM sys_dict_item WHERE id = ? LIMIT 1";
        List<SysDictItem> list = jdbcTemplate.query(sql, MAPPER, id);
        return list.stream().findFirst();
    }

    /** 同类型内 item_key 是否已存在（excludeId 用于编辑时排除自身） */
    public boolean existsKey(String dictCode, String itemKey, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM sys_dict_item WHERE dict_code = ? AND item_key = ?"
                + (excludeId == null ? "" : " AND id <> ?");
        Integer n = excludeId == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, dictCode, itemKey)
                : jdbcTemplate.queryForObject(sql, Integer.class, dictCode, itemKey, excludeId);
        return n != null && n > 0;
    }

    /** 该类型字典项总数 */
    public int countByDictCode(String dictCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_dict_item WHERE dict_code = ?", Integer.class, dictCode);
        return n == null ? 0 : n;
    }

    /** 该类型启用中（status=1）的字典项数 */
    public int countEnabledByDictCode(String dictCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_dict_item WHERE dict_code = ? AND status = 1",
                Integer.class, dictCode);
        return n == null ? 0 : n;
    }

    /** 新增字典项，返回受影响行数 */
    public int insert(SysDictItem item) {
        String sql = "INSERT INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                item.getDictCode(),
                item.getItemKey(),
                item.getItemLabel(),
                item.getItemValue(),
                item.getExtra(),
                item.getSort(),
                item.getStatus());
    }

    /** 按主键改业务字段；updated_at 由数据库 ON UPDATE 刷新 */
    public int update(SysDictItem item) {
        String sql = "UPDATE sys_dict_item SET item_key = ?, item_label = ?, item_value = ?, "
                + "extra = ?, sort = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                item.getItemKey(),
                item.getItemLabel(),
                item.getItemValue(),
                item.getExtra(),
                item.getSort(),
                item.getStatus(),
                item.getId());
    }

    /** 物理删除单个字典项 */
    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM sys_dict_item WHERE id = ?", id);
    }

    /** 物理删除某类型下全部字典项（删类型前的同事务级联清理） */
    public int deleteByDictCode(String dictCode) {
        return jdbcTemplate.update("DELETE FROM sys_dict_item WHERE dict_code = ?", dictCode);
    }
}
