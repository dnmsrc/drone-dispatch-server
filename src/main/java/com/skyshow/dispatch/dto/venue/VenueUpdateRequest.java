package com.skyshow.dispatch.dto.venue;

import java.math.BigDecimal;

/**
 * 修改场地请求体：只允许改 4 个业务字段。
 * 改哪一条由 URL 上的 id 决定（@PathVariable），不放进 DTO；
 * enabled、时间戳、软删除等系统字段同样不接收。
 */
public record VenueUpdateRequest(
        String name,
        String address,
        BigDecimal lng,
        BigDecimal lat) {
}
