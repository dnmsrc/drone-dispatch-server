package com.skyshow.dispatch.dto.schedule;

import java.util.List;

/**
 * 单日时段保存请求体（PUT /api/schedule/days/{date}/slots）。
 *
 * @param slots 当日时段；null 表示恢复全局默认，空列表表示当日不接单
 * @param note  可选说明（节日 / 备注），可空
 */
public record DaySlotsRequest(
        List<String> slots,
        String note) {
}
