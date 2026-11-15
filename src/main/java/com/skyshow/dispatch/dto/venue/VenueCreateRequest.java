package com.skyshow.dispatch.dto.venue;

import java.math.BigDecimal;

/**
 * 新增场地请求体：接收前端新增表单提交的字段。
 * id、时间戳、软删除标记等系统字段不接收；enabled 由表单开关提交，
 * 不传时由 Service 兜底为启用（对齐前端开关默认打开）。
 *
 * <p>必填仅 name（场地名称）与经纬度 lng/lat；address（详细地址）为选填，
 * 为空时由 Service 归一空串落库（venues.address 列仍保持 NOT NULL）。
 */
public record VenueCreateRequest(
                String name,
                String address,
                BigDecimal lng,
                BigDecimal lat,
                Boolean enabled) {
}
