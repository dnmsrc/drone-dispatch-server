package com.skyshow.dispatch.dto.schedule;

import java.util.List;

/**
 * 月历视图：区间内每一天 + 当前全局默认时段（弹窗初始值）。
 */
public record ScheduleMonthVO(
        List<ScheduleCellVO> days,
        List<String> defaultSlots,
        Integer bookingWindowDays) {
}
