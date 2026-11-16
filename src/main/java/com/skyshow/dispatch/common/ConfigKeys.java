package com.skyshow.dispatch.common;

/**
 * sys_config 配置键统一定义，避免各 Service 散落魔法字符串。
 *
 * <p>命名沿用下划线风格，与数据库 config_key 口径一致；
 * 计价标量加 {@code pricing.} 前缀、档期加 {@code schedule.} 前缀，按领域分组。
 */
public final class ConfigKeys {

    private ConfigKeys() {
    }

    /* ── 计价域标量（pricing_rule 表之外的两个定制收费）── */
    /** 定制文字价格（元） */
    public static final String PRICING_CUSTOM_TEXT = "pricing.custom_text_price";
    /** LOGO / 图片定制价格（元） */
    public static final String PRICING_CUSTOM_LOGO = "pricing.custom_logo_price";

    /* ── 支付域 ── */
    /** 下单后支付时限（分钟），超时未付自动取消 */
    public static final String PAY_EXPIRE_MINUTES = "pay_expire_minutes";

    /* ── 档期域全局默认 ── */
    /** 全局默认可接单时段（JSON 数组，元素形如 "19:00-20:00"） */
    public static final String SCHEDULE_DEFAULT_SLOTS = "schedule.default_slots";
    /** 预约窗口（天）：用户最多可预约未来 N 天 */
    public static final String SCHEDULE_BOOKING_WINDOW = "schedule.booking_window_days";
}
