package com.skyshow.dispatch.dto.venue;

import java.math.BigDecimal;

/**
 * 修改场地请求体：接收前端编辑表单提交的字段。
 * 改哪一条由 URL 上的 id 决定（{@code @PathVariable}），不放进 DTO；
 * 时间戳、软删除等系统字段同样不接收。
 *
 * <p>enabled 随编辑表单的启用开关一起提交（前端必传 true/false），
 * 后端据此更新启用状态；缺失时由 Service 按 400 拒绝，避免状态被静默置空。
 */
public record VenueUpdateRequest(
                String name,
                String address,
                BigDecimal lng,
                BigDecimal lat,
                Boolean enabled) {
}
