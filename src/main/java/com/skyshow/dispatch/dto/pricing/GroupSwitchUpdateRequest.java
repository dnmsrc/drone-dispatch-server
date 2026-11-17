package com.skyshow.dispatch.dto.pricing;

/**
 * 计价分组总闸更新请求体（PUT /api/pricing/groups/{group}/enabled）。
 *
 * <p>用包装类型 {@link Boolean} 而非基本类型 boolean：service 需区分「请求体未带 enabled(null)」
 * 与「显式传 false」，null 一律按 400 拦截，避免误把缺省当成关闭整组。
 *
 * @param enabled 目标开关状态（true 启用 / false 停用），不允许为 null
 */
public record GroupSwitchUpdateRequest(
        Boolean enabled) {
}
