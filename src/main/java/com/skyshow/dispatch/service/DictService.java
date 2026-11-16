package com.skyshow.dispatch.service;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.dto.dict.DictItemSaveRequest;
import com.skyshow.dispatch.dto.dict.DictItemVO;
import com.skyshow.dispatch.dto.dict.DictTypeCreateRequest;
import com.skyshow.dispatch.dto.dict.DictTypeDetailVO;
import com.skyshow.dispatch.dto.dict.DictTypeSummaryVO;
import com.skyshow.dispatch.dto.dict.DictTypeUpdateRequest;
import com.skyshow.dispatch.entity.SysDict;
import com.skyshow.dispatch.entity.SysDictItem;
import com.skyshow.dispatch.repository.SysDictItemRepository;
import com.skyshow.dispatch.repository.SysDictRepository;
import com.skyshow.dispatch.security.LoginUser;
import com.skyshow.dispatch.security.LoginUserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 字典业务服务：字典类型 + 字典项两级 CRUD。
 *
 * <p>权限：读接口登录即可，写接口仅 ADMIN（{@link #requireAdmin()} 统一闸门）。
 * 删除策略：字典/配置类物理删除；内置类型（system_flag=1）禁删类型，仅可停用/改字典项。
 */
@Service
public class DictService {

    /** 管理员角色码（与 admin_users.role、JWT claim 口径一致） */
    private static final String ROLE_ADMIN = "ADMIN";

    /** 类型标识规则：小写字母开头，仅小写英文/数字/下划线，2-32 位 */
    private static final Pattern DICT_CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,31}$");

    private final SysDictRepository dictRepository;
    private final SysDictItemRepository itemRepository;

    public DictService(SysDictRepository dictRepository, SysDictItemRepository itemRepository) {
        this.dictRepository = dictRepository;
        this.itemRepository = itemRepository;
    }

    /* ═══════════════════ 字典类型 ═══════════════════ */

    /** 类型列表（汇总条目数/启用数），登录即可读 */
    public List<DictTypeSummaryVO> listTypes() {
        return dictRepository.findAll().stream().map(this::toSummaryVO).toList();
    }

    /** 新增自定义类型，仅 ADMIN */
    public void createType(DictTypeCreateRequest request) {
        requireAdmin();
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        String code = trim(request.dictCode());
        if (!DICT_CODE_PATTERN.matcher(code).matches()) {
            throw new BizException(400, "类型标识需为小写字母开头的英文/数字/下划线（2-32 位）");
        }
        if (dictRepository.existsByCode(code)) {
            throw new BizException(400, "类型标识已存在");
        }
        String name = requireName(request.dictName());

        SysDict dict = new SysDict();
        dict.setDictCode(code);
        dict.setDictName(name);
        dict.setRemark(trimToEmpty(request.remark()));
        // 新建恒为自定义类型，system_flag 在 Repository 写死为 0
        dictRepository.insert(dict);
    }

    /** 修改类型名/备注（dict_code 不可改），仅 ADMIN */
    public void updateType(String dictCode, DictTypeUpdateRequest request) {
        requireAdmin();
        SysDict dict = requireType(dictCode);
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        String name = requireName(request.dictName());
        dictRepository.update(dict.getDictCode(), name, trimToEmpty(request.remark()));
    }

    /** 物理删除整组：内置类型拒绝；非内置同事务先清字典项再删类型，仅 ADMIN */
    @Transactional
    public void deleteType(String dictCode) {
        requireAdmin();
        SysDict dict = requireType(dictCode);
        if (dict.getSystemFlag() != null && dict.getSystemFlag() == 1) {
            throw new BizException(400, "系统内置字典被业务引用，不可删除（可停用或修改字典项）");
        }
        // 先删子项解除外键 fk_item_dict，再删类型；同事务保证两者一起成功或一起回滚
        itemRepository.deleteByDictCode(dict.getDictCode());
        dictRepository.deleteByCode(dict.getDictCode());
    }

    /* ═══════════════════ 字典项 ═══════════════════ */

    /** 某类型详情 + 全部字典项，登录即可读 */
    public DictTypeDetailVO getTypeDetail(String dictCode) {
        SysDict dict = requireType(dictCode);
        List<DictItemVO> items = itemRepository.findByDictCode(dictCode).stream()
                .map(this::toItemVO)
                .toList();
        return new DictTypeDetailVO(
                dict.getDictCode(),
                dict.getDictName(),
                trimToEmpty(dict.getRemark()),
                dict.getSystemFlag(),
                items.size(),
                (int) items.stream().filter(i -> i.status() != null && i.status() == 1).count(),
                items);
    }

    /** 新增字典项，仅 ADMIN */
    public void createItem(String dictCode, DictItemSaveRequest request) {
        requireAdmin();
        SysDict dict = requireType(dictCode);
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        String key = requireKey(request.itemKey());
        if (itemRepository.existsKey(dict.getDictCode(), key, null)) {
            throw new BizException(400, "键「" + key + "」已存在");
        }
        String label = requireLabel(request.itemLabel());

        SysDictItem item = new SysDictItem();
        item.setDictCode(dict.getDictCode());
        item.setItemKey(key);
        item.setItemLabel(label);
        item.setItemValue(normalizeValue(request.itemValue()));
        item.setExtra(trimToEmpty(request.extra()));
        // 排序缺省 = 当前项数 + 1
        item.setSort(request.sort() != null ? request.sort() : itemRepository.countByDictCode(dict.getDictCode()) + 1);
        item.setStatus(normalizeStatus(request.status()));
        itemRepository.insert(item);
    }

    /** 修改字典项（含启停，开关仅传 status 也走这里），仅 ADMIN */
    public void updateItem(String dictCode, Long id, DictItemSaveRequest request) {
        requireAdmin();
        requireType(dictCode);
        SysDictItem item = itemRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "字典项不存在"));
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        String key = requireKey(request.itemKey());
        // 改键时仍须同类型唯一（排除自身）
        if (!key.equals(item.getItemKey())
                && itemRepository.existsKey(dictCode, key, id)) {
            throw new BizException(400, "键「" + key + "」已存在");
        }
        String label = requireLabel(request.itemLabel());

        item.setItemKey(key);
        item.setItemLabel(label);
        item.setItemValue(normalizeValue(request.itemValue()));
        item.setExtra(trimToEmpty(request.extra()));
        item.setSort(request.sort() != null ? request.sort() : item.getSort());
        item.setStatus(normalizeStatus(request.status()));
        itemRepository.update(item);
    }

    /** 物理删除单个字典项（内置类型的字典项同样允许，对齐原型），仅 ADMIN */
    public void deleteItem(String dictCode, Long id) {
        requireAdmin();
        requireType(dictCode);
        SysDictItem item = itemRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "字典项不存在"));
        itemRepository.deleteById(item.getId());
    }

    /* ═══════════════════ 私有校验/转换 ═══════════════════ */

    /** 写操作角色闸门：非 ADMIN 一律 403 */
    private void requireAdmin() {
        LoginUser user = LoginUserHolder.get();
        if (user == null) {
            throw new BizException(401, "未登录或登录已失效，请重新登录");
        }
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new BizException(403, "无权限：仅管理员可修改字典配置");
        }
    }

    /** 类型必须存在，否则 404 */
    private SysDict requireType(String dictCode) {
        return dictRepository.findByCode(trim(dictCode))
                .orElseThrow(() -> new BizException(404, "字典类型不存在"));
    }

    private String requireName(String name) {
        String n = trim(name);
        if (n.isEmpty()) {
            throw new BizException(400, "请填写类型名称");
        }
        return n;
    }

    private String requireKey(String key) {
        String k = trim(key);
        if (k.isEmpty()) {
            throw new BizException(400, "请填写键");
        }
        return k;
    }

    private String requireLabel(String label) {
        String l = trim(label);
        if (l.isEmpty()) {
            throw new BizException(400, "请填写标签");
        }
        return l;
    }

    /** 数值缺省归 0，绝不落 null（与种子口径一致，前端也按 number 使用） */
    private BigDecimal normalizeValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** 状态只接受 0/1，缺省按启用 1 */
    private Integer normalizeStatus(Integer status) {
        return status != null && status == 0 ? 0 : 1;
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** null/空白统一归一空串，避免落库 null 与前端兜底分叉 */
    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /* ═══════════════════ Entity → VO ═══════════════════ */

    private DictTypeSummaryVO toSummaryVO(SysDict dict) {
        String code = dict.getDictCode();
        return new DictTypeSummaryVO(
                code,
                dict.getDictName(),
                trimToEmpty(dict.getRemark()),
                dict.getSystemFlag(),
                itemRepository.countByDictCode(code),
                itemRepository.countEnabledByDictCode(code));
    }

    private DictItemVO toItemVO(SysDictItem item) {
        return new DictItemVO(
                item.getId(),
                item.getItemKey(),
                item.getItemLabel(),
                item.getItemValue() == null ? BigDecimal.ZERO : item.getItemValue(),
                trimToEmpty(item.getExtra()),
                item.getSort(),
                item.getStatus());
    }
}
