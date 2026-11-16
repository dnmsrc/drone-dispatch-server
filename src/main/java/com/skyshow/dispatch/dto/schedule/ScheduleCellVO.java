package com.skyshow.dispatch.dto.schedule;

import java.util.List;
import java.util.Map;

/**
 * 月历中单日视图（管理端档期页 + H5 共用核心字段）。
 *
 * @param date            YYYY-MM-DD
 * @param slots           当日实际时段（回退全局默认后的结果）
 * @param openSlots       可预约时段 = slots 过滤掉已订满（计数 ≥ 1）的时段
 * @param bookedSlots     各时段已订数，键为时段串
 * @param limit           当日名额 = slots 时段数（休整日为 0），由时段派生
 * @param closed          是否休整日
 * @param slotsOverridden 当日时段是否被单独配置过（区别于沿用默认）
 * @param note            休整 / 节日说明
 */
public record ScheduleCellVO(
        String date,
        List<String> slots,
        List<String> openSlots,
        Map<String, Integer> bookedSlots,
        int limit,
        boolean closed,
        boolean slotsOverridden,
        String note) {
}
