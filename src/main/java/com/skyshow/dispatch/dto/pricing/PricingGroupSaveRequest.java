package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整组保存请求体（PUT /api/pricing/groups/{group}）。
 * 服务端按列表顺序重排 sort、归一 status，并校验组内 item_key 唯一。
 */
public record PricingGroupSaveRequest(
        List<Item> items) {

    /** 整组保存里的单行；id 可空（视为新增），有 id 视为更新已存在行。 */
    public record Item(
            Long id,
            String itemKey,
            String itemLabel,
            BigDecimal itemValue,
            String extra,
            Integer status) {
    }
}
