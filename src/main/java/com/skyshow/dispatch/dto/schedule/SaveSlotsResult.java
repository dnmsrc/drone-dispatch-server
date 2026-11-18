package com.skyshow.dispatch.dto.schedule;

import java.util.List;

/**
 * 单日时段保存结果（PUT /api/schedule/days/{date}/slots）。
 *
 * <p>已订时段（客户已付款买断的名额）不允许被单方面撤销，服务端会自动并入并在此回报，
 * 前端据此提示「为什么某天没改成」。并入后与请求时段重叠时整批 400，不会写半截。
 *
 * @param date    变更日期，YYYY-MM-DD
 * @param slots   保存后该日实际生效的时段（已含被强制保留的已订时段）
 * @param blocked 因已有订单被强制保留、未能随请求移除的时段明细
 */
public record SaveSlotsResult(
        String date,
        List<String> slots,
        List<BlockedSlot> blocked) {

    /**
     * 被保留的已订时段。
     *
     * @param slot   时段串，如 19:00-20:00
     * @param orders 该时段有效订单数
     */
    public record BlockedSlot(String slot, int orders) {
    }
}