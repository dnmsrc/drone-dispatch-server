package com.skyshow.dispatch.dto.schedule;

/**
 * 休整日设置请求体（PUT /api/schedule/days/{date}/closed）。
 *
 * @param closed true 设为休整日（全停接）；false 恢复营业
 * @param note   休整说明，可空
 */
public record DayClosedRequest(
        Boolean closed,
        String note) {
}
