package com.skyshow.dispatch.dto.pricing;

import java.math.BigDecimal;

/**
 * 定制收费标量保存请求体（PUT /api/pricing/custom）。
 * 两个字段均可空：传哪个改哪个，未传保留原值；范围 0-100000。
 */
public record CustomFeeRequest(
        BigDecimal customTextPrice,
        BigDecimal customLogoPrice) {
}
