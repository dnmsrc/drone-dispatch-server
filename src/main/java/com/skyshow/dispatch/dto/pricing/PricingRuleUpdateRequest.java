package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;

/**
 * 单项编辑 / 启停请求体（PUT /api/pricing/rules/{id}）。
 * 开关场景仅传 status 也可走此端点；itemKey 等其它字段为 null 时由 Service 保留原值。
 */
public record PricingRuleUpdateRequest(
        String itemKey,
        String itemLabel,
        BigDecimal itemValue,
        String extra,
        Integer sort,
        Integer status) {
}
