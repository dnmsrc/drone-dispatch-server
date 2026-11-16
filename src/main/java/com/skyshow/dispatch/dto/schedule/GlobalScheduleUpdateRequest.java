package com.skyshow.dispatch.dto.schedule;

import java.util.List;

/**
 * 全局档期设置保存请求体；两字段均可空，传哪个改哪个。
 */
public record GlobalScheduleUpdateRequest(
        List<String> defaultSlots,
        Integer bookingWindowDays) {
}
