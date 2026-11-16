package com.skyshow.dispatch.dto.payment;

/**
 * 支付配置保存请求体（PUT /api/payment-config）。
 *
 * @param payExpireMinutes 下单后支付时限（分钟），范围 1-120，超时未付自动取消
 */
public record PaymentConfigUpdateRequest(
        Integer payExpireMinutes) {
}
