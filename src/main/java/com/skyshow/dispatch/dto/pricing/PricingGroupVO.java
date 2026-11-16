package com.skyshow.dispatch.dto.pricing;

import java.util.List;

/**
 * 管理端「计价管理」整组视图：一个分组及其全部规则（含停用项，按 sort 升序）。
 */
public record PricingGroupVO(
        String groupCode,
        List<PricingRuleVO> items) {
}
