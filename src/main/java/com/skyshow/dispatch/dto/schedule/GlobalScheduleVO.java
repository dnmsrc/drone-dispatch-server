package com.skyshow.dispatch.dto.schedule;

import java.util.List;

/**
 * 全局档期设置出参 / 保存请求共用结构。
 *
 * @param defaultSlots      全局默认可接单时段（整点区间串）
 * @param bookingWindowDays 预约窗口（天）：用户最多可预约未来 N 天，范围 7-365
 */
public record GlobalScheduleVO(
        List<String> defaultSlots,
        Integer bookingWindowDays) {
}
