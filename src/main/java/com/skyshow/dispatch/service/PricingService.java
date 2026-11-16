package com.skyshow.dispatch.service;

import com.skyshow.dispatch.common.BizException;
import com.skyshow.dispatch.common.ConfigKeys;
import com.skyshow.dispatch.dto.pricing.CustomFeeRequest;
import com.skyshow.dispatch.dto.pricing.CustomFeeVO;
import com.skyshow.dispatch.dto.pricing.PricingGroupSaveRequest;
import com.skyshow.dispatch.dto.pricing.PricingGroupVO;
import com.skyshow.dispatch.dto.pricing.PricingPublicVO;
import com.skyshow.dispatch.dto.pricing.PricingRuleUpdateRequest;
import com.skyshow.dispatch.dto.pricing.PricingRuleVO;
import com.skyshow.dispatch.dto.pricing.RefundRuleVO;
import com.skyshow.dispatch.entity.PricingRule;
import com.skyshow.dispatch.repository.PricingRuleRepository;
import com.skyshow.dispatch.repository.SysConfigRepository;
import com.skyshow.dispatch.security.LoginUser;
import com.skyshow.dispatch.security.LoginUserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 计价业务服务：管理端维护 5 组计价规则与两个定制标量，H5 只读启用项。
 *
 * <p>权限：读接口登录即可；写接口仅 ADMIN（{@link #requireAdmin()} 统一闸门）。
 * 分组固定为 {@link #GROUPS}，不接受动态分组，与原型 pricingRules 的 PRICING_GROUPS 对齐。
 */
@Service
public class PricingService {

    private static final String ROLE_ADMIN = "ADMIN";

    /** 合法计价分组白名单（refund 即原字典 refund_rule） */
    private static final Set<String> GROUPS = Set.of("tier", "duration", "addon", "pattern", "refund");

    /** 定制价 / 支付时限缺省值（sys_config 尚未写入时兜底，对齐原型种子） */
    private static final BigDecimal DEFAULT_TEXT_PRICE = new BigDecimal("2000");
    private static final BigDecimal DEFAULT_LOGO_PRICE = new BigDecimal("5000");
    private static final int DEFAULT_PAY_EXPIRE = 15;
    /** 定制价上限（元） */
    private static final BigDecimal CUSTOM_FEE_MAX = new BigDecimal("100000");

    private final PricingRuleRepository ruleRepository;
    private final SysConfigRepository configRepository;

    public PricingService(PricingRuleRepository ruleRepository, SysConfigRepository configRepository) {
        this.ruleRepository = ruleRepository;
        this.configRepository = configRepository;
    }

    /* ═══════════════════ 管理端：分组视图 ═══════════════════ */

    /** 全部分组的管理端视图（含停用项），按固定分组顺序返回 */
    public List<PricingGroupVO> listAllGroups() {
        List<PricingGroupVO> result = new ArrayList<>();
        for (String group : List.of("tier", "duration", "addon", "pattern", "refund")) {
            result.add(new PricingGroupVO(group, toVOList(ruleRepository.findByGroup(group))));
        }
        return result;
    }

    /** 单个分组的管理端视图（含停用项） */
    public PricingGroupVO getGroup(String group) {
        requireGroup(group);
        return new PricingGroupVO(group, toVOList(ruleRepository.findByGroup(group)));
    }

    /**
     * 整组保存：先完整校验入参，再在同一事务内清空该组、按列表顺序重插，sort 即列表序号。
     * 规则表无任何外键引用其主键（订单存价格快照），故重建换 id 无业务损害，
     * 同时天然保证组内排序与 item_key 唯一。
     */
    @Transactional
    public void replaceGroup(String group, PricingGroupSaveRequest request) {
        requireAdmin();
        requireGroup(group);
        if (request == null || request.items() == null) {
            throw new BizException(400, "请求体不能为空");
        }

        // 1) 先在内存里把整组规则构建并校验完，任何一行不合法都直接抛错，不碰数据库
        List<PricingRule> toSave = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        int sort = 1;
        for (PricingGroupSaveRequest.Item item : request.items()) {
            String key = trim(item.itemKey());
            String label = trim(item.itemLabel());
            if (key.isEmpty() || label.isEmpty()) {
                throw new BizException(400, "每项的键与文案均不能为空");
            }
            if (!seenKeys.add(key)) {
                throw new BizException(400, "组内键「" + key + "」重复");
            }
            PricingRule rule = new PricingRule();
            rule.setGroupCode(group);
            rule.setItemKey(key);
            rule.setItemLabel(label);
            rule.setItemValue(normalizeValue(item.itemValue()));
            rule.setExtra(trimToEmpty(item.extra()));
            rule.setSort(sort++);
            rule.setStatus(normalizeStatus(item.status()));
            toSave.add(rule);
        }

        // 2) 校验全通过，事务内清空旧组并重插（同事务保证要么全成功、要么一起回滚）
        ruleRepository.deleteByGroup(group);
        for (PricingRule rule : toSave) {
            ruleRepository.insert(rule);
        }
    }

    /* ═══════════════════ 管理端：单项编辑 / 启停 ═══════════════════ */

    /** 单项更新（开关仅传 status 也走这里）；字段为 null 保留原值，仅 ADMIN */
    public void updateRule(Long id, PricingRuleUpdateRequest request) {
        requireAdmin();
        PricingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BizException(404, "计价规则不存在"));
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }

        if (request.itemKey() != null) {
            String key = trim(request.itemKey());
            if (key.isEmpty()) {
                throw new BizException(400, "键不能为空");
            }
            if (!key.equals(rule.getItemKey())
                    && ruleRepository.existsKey(rule.getGroupCode(), key, id)) {
                throw new BizException(400, "键「" + key + "」在该分组已存在");
            }
            rule.setItemKey(key);
        }
        if (request.itemLabel() != null) {
            String label = trim(request.itemLabel());
            if (label.isEmpty()) {
                throw new BizException(400, "文案不能为空");
            }
            rule.setItemLabel(label);
        }
        if (request.itemValue() != null) {
            rule.setItemValue(normalizeValue(request.itemValue()));
        }
        if (request.extra() != null) {
            rule.setExtra(trimToEmpty(request.extra()));
        }
        if (request.sort() != null) {
            if (request.sort() < 0) {
                throw new BizException(400, "排序号需为不小于 0 的整数");
            }
            rule.setSort(request.sort());
        }
        if (request.status() != null) {
            rule.setStatus(normalizeStatus(request.status()));
        }
        ruleRepository.update(rule);
    }

    /* ═══════════════════ 管理端：定制收费标量 ═══════════════════ */

    /** 读两个定制价（缺失给默认值） */
    public CustomFeeVO getCustomFees() {
        return new CustomFeeVO(
                readDecimal(ConfigKeys.PRICING_CUSTOM_TEXT, DEFAULT_TEXT_PRICE),
                readDecimal(ConfigKeys.PRICING_CUSTOM_LOGO, DEFAULT_LOGO_PRICE));
    }

    /** 保存定制价：传哪个改哪个，范围 0-100000，仅 ADMIN */
    public void saveCustomFees(CustomFeeRequest request) {
        requireAdmin();
        if (request == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (request.customTextPrice() != null) {
            configRepository.upsertValue(ConfigKeys.PRICING_CUSTOM_TEXT,
                    checkCustomFee(request.customTextPrice(), "定制文字价格").toPlainString());
        }
        if (request.customLogoPrice() != null) {
            configRepository.upsertValue(ConfigKeys.PRICING_CUSTOM_LOGO,
                    checkCustomFee(request.customLogoPrice(), "LOGO 定制价格").toPlainString());
        }
    }

    /* ═══════════════════ H5：只读启用视图 ═══════════════════ */

    /**
     * H5 下单计价视图：只含启用项，字段对齐原型 priceRules，一次拿全下单所需规则。
     * 时段不在此返回（P4 决策砍掉 timeSlots，时段由档期 openSlots 驱动）。
     */
    public PricingPublicVO getPublicPricing() {
        CustomFeeVO fees = getCustomFees();
        List<PricingRule> tiers = ruleRepository.findEnabledByGroup("tier");
        List<PricingRule> durations = ruleRepository.findEnabledByGroup("duration");
        List<PricingRule> addons = ruleRepository.findEnabledByGroup("addon");
        List<PricingRule> patterns = ruleRepository.findEnabledByGroup("pattern");

        return new PricingPublicVO(
                tiers.stream().map(r -> new PricingPublicVO.Tier(
                        parseIntKey(r.getItemKey()), r.getItemValue(), trimToEmpty(r.getExtra()))).toList(),
                durations.stream().map(r -> new PricingPublicVO.Duration(
                        parseIntKey(r.getItemKey()), r.getItemValue(), r.getItemLabel())).toList(),
                fees.customTextPrice(),
                fees.customLogoPrice(),
                addons.stream().map(r -> new PricingPublicVO.Addon(
                        r.getItemKey(), r.getItemLabel(), r.getItemValue())).toList(),
                patterns.stream().map(r -> new PricingPublicVO.Pattern(
                        r.getItemKey(), r.getItemLabel())).toList(),
                readInt(ConfigKeys.PAY_EXPIRE_MINUTES, DEFAULT_PAY_EXPIRE));
    }

    /** 退款阶梯只读视图：启用项按 minHours 降序 */
    public List<RefundRuleVO> getRefundRules() {
        return ruleRepository.findEnabledByGroup("refund").stream()
                .map(r -> new RefundRuleVO(parseIntKey(r.getItemKey()), r.getItemValue(), r.getItemLabel()))
                .sorted((a, b) -> Integer.compare(b.minHours(), a.minHours()))
                .toList();
    }

    /* ═══════════════════ 私有校验 / 转换 ═══════════════════ */

    /** 写操作角色闸门：非 ADMIN 一律 403 */
    private void requireAdmin() {
        LoginUser user = LoginUserHolder.get();
        if (user == null) {
            throw new BizException(401, "未登录或登录已失效，请重新登录");
        }
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new BizException(403, "无权限：仅管理员可修改计价规则");
        }
    }

    /** 分组必须在白名单内，否则 400 */
    private void requireGroup(String group) {
        if (group == null || !GROUPS.contains(group)) {
            throw new BizException(400, "非法的计价分组：" + group);
        }
    }

    private BigDecimal checkCustomFee(BigDecimal value, String field) {
        if (value.signum() < 0 || value.compareTo(CUSTOM_FEE_MAX) > 0) {
            throw new BizException(400, field + "需在 0-100000 之间");
        }
        return value;
    }

    /** 数值缺省归 0，绝不落 null（表列 NOT NULL） */
    private BigDecimal normalizeValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** 状态只接受 0/1，缺省按启用 1 */
    private Integer normalizeStatus(Integer status) {
        return status != null && status == 0 ? 0 : 1;
    }

    /** 从 sys_config 读 decimal 配置，缺失或非法时给默认值 */
    private BigDecimal readDecimal(String key, BigDecimal fallback) {
        return configRepository.findValue(key)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(BigDecimal::new)
                .orElse(fallback);
    }

    /** 从 sys_config 读 int 配置，缺失或非法时给默认值 */
    private int readInt(String key, int fallback) {
        return configRepository.findValue(key)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .orElse(fallback);
    }

    private Integer parseIntKey(String key) {
        try {
            return Integer.valueOf(key.trim());
        } catch (NumberFormatException e) {
            throw new BizException(500, "计价规则键「" + key + "」应为整数");
        }
    }

    private List<PricingRuleVO> toVOList(List<PricingRule> rules) {
        return rules.stream().map(r -> new PricingRuleVO(
                r.getId(), r.getGroupCode(), r.getItemKey(), r.getItemLabel(),
                r.getItemValue(), trimToEmpty(r.getExtra()), r.getSort(), r.getStatus())).toList();
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
