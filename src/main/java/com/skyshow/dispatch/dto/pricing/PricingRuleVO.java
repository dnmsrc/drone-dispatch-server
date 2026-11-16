package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;

/**
 * 计价规则出参（管理端视图，含停用项）。
 * 字段名贴前端原型 pricingRules 的通用行结构；前端适配层可按分组再映射为 drones/minutes 等领域字段。
 */
public record PricingRuleVO(
        Long id,
        String groupCode,
        String itemKey,
        String itemLabel,
        BigDecimal itemValue,
        String extra,
        Integer sort,
        Integer status) {
}
