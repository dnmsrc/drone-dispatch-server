package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * H5 用户端计价只读视图（GET /api/pricing/public），只含启用项。
 *
 * <p>字段名对齐原型 {@code GET /api/config/pricing} 的 priceRules 形状，H5 拉取后零改名可用。
 * 正式版按决策 P4 不再返回 timeSlots（时段改由档期 openSlots 驱动），故此处无该字段。
 * payExpireMinutes 来自支付配置，由上层装配，保证 H5 一次请求拿全下单所需规则。
 */
public record PricingPublicVO(
        List<Tier> tiers,
        List<Duration> durations,
        BigDecimal customTextPrice,
        BigDecimal customLogoPrice,
        List<Addon> addons,
        List<Pattern> patterns,
        Integer payExpireMinutes) {

    /** 架次档位：item_key=架次数，item_value=基础价，extra=适用场景 */
    public record Tier(Integer drones, BigDecimal price, String desc) {
    }

    /** 表演时长：item_key=分钟数，item_value=加价（0=含基础价），item_label=展示文案 */
    public record Duration(Integer minutes, BigDecimal extra, String label) {
    }

    /** 附加服务：item_key=英文键，item_label=名称，item_value=加价 */
    public record Addon(String key, String label, BigDecimal price) {
    }

    /** 预设图案（不加价）：item_key=英文键，item_label=名称 */
    public record Pattern(String key, String label) {
    }
}
