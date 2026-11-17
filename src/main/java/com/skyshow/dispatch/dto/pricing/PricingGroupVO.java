package com.skyshow.dispatch.dto.pricing;

import java.util.List;

/**
 * 管理端「计价管理」整组视图：一个分组、其分组总闸状态及全部规则（含停用项，按 sort 升序）。
 *
 * @param groupCode 后端单数分组码：tier/duration/addon/pattern/refund
 * @param enabled   分组总闸（sys_config 布尔，fail-open 缺省 true）；关闭仅影响 H5 是否可选，
 *                  不影响本视图返回全部行，也不改动各行 status
 * @param items     该组全部规则行（含 status=0 的停用行），管理端始终全量返回
 */
public record PricingGroupVO(
        String groupCode,
        boolean enabled,
        List<PricingRuleVO> items) {
}
