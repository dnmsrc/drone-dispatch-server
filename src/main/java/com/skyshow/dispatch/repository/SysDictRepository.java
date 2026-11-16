package com.skyshow.dispatch.repository;

import com.skyshow.dispatch.entity.SysDict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** sys_dict 字典类型数据访问：只写 SQL 与行映射，不含业务规则。 */
@Repository
public class SysDictRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysDictRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 一行查询结果 → 一个 SysDict 对象；无状态公式，全类复用 */
    private static final RowMapper<SysDict> MAPPER = (rs, rowNum) -> {
        SysDict dict = new SysDict();
        dict.setId(rs.getLong("id"));
        dict.setDictCode(rs.getString("dict_code"));
        dict.setDictName(rs.getString("dict_name"));
        dict.setSystemFlag(rs.getInt("system_flag"));
        dict.setRemark(rs.getString("remark"));
        dict.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        dict.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return dict;
    };

    /** 明确列出查询列，不用 SELECT * */
    private static final String COLUMNS = "id, dict_code, dict_name, system_flag, remark, created_at, updated_at";

    /** 按字典编码查单个类型，找不到返回 Optional.empty() */
    public Optional<SysDict> findByCode(String dictCode) {
        String sql = "SELECT " + COLUMNS + " FROM sys_dict WHERE dict_code = ? LIMIT 1";
        List<SysDict> list = jdbcTemplate.query(sql, MAPPER, dictCode);
        return list.stream().findFirst();
    }

    /** 是否存在该编码（新增唯一校验用） */
    public boolean existsByCode(String dictCode) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_dict WHERE dict_code = ?", Integer.class, dictCode);
        return n != null && n > 0;
    }

    /** 全部字典类型，按 dict_code 升序 */
    public List<SysDict> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM sys_dict ORDER BY dict_code";
        return jdbcTemplate.query(sql, MAPPER);
    }

    /** 新增自定义类型（system_flag 恒为 0） */
    public int insert(SysDict dict) {
        String sql = "INSERT INTO sys_dict (dict_code, dict_name, system_flag, remark) VALUES (?, ?, 0, ?)";
        return jdbcTemplate.update(sql, dict.getDictCode(), dict.getDictName(), dict.getRemark());
    }

    /** 改类型名/备注；dict_code 不可改，updated_at 由数据库 ON UPDATE 刷新 */
    public int update(String dictCode, String dictName, String remark) {
        String sql = "UPDATE sys_dict SET dict_name = ?, remark = ? WHERE dict_code = ?";
        return jdbcTemplate.update(sql, dictName, remark, dictCode);
    }

    /** 物理删除类型（调用方须先删其下字典项，否则外键 fk_item_dict 拦截） */
    public int deleteByCode(String dictCode) {
        return jdbcTemplate.update("DELETE FROM sys_dict WHERE dict_code = ?", dictCode);
    }
}
