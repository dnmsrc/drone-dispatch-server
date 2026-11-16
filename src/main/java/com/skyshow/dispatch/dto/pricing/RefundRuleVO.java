package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;

/**
 * 退款阶梯只读视图（refund 分组启用项，按 minHours 降序）。
 */
public record RefundRuleVO(
        Integer minHours,
        BigDecimal ratio,
        String label) {
}
