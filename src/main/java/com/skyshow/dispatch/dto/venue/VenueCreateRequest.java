package com.skyshow.dispatch.dto.venue;

import java.math.BigDecimal;

/**
 * 新增场地请求体：只接收允许前端提交的 4 个字段，
 * id、enabled、时间戳、软删除标记等系统字段一律不接收。
 *
 * <p>必填仅 name（场地名称）与经纬度 lng/lat；address（详细地址）为选填，
 * 为空时由 Service 归一空串落库（venues.address 列仍保持 NOT NULL）。
 */
public record VenueCreateRequest(
                String name,
                String address,
                BigDecimal lng,
                BigDecimal lat) {
}