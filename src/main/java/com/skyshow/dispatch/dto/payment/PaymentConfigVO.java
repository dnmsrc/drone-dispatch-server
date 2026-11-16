package com.skyshow.dispatch.dto.payment;

/**
 * 支付配置出参（管理端支付管理页）。
 *
 * <p>本期只承载支付时限；在线支付渠道开关、收款账户按规划在正式支付模块再开放，
 * 不提前暴露成可操作的假功能（决策 P5）。
 */
public record PaymentConfigVO(
        Integer payExpireMinutes) {
}
