package com.skyshow.dispatch.dto.pricing;

/**
 * 计价分组总闸状态视图（GET /api/pricing/group-status，H5 匿名可读）。
 *
 * <p>H5 下单前先拉本视图，仅对 {@code enabled=true} 的分组再请求对应计价数据，
 * 停用分组不发后续请求。管理端整组视图 {@link PricingGroupVO} 也带同名字段，口径一致。
 *
 * @param groupCode 后端单数分组码：tier/duration/addon/pattern/refund
 * @param enabled   分组总闸；fail-open，配置缺失时按 true 返回
 */
public record GroupStatusVO(
        String groupCode,
        boolean enabled) {
}
