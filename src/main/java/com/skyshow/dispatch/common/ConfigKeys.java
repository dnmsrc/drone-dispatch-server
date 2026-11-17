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

    /* ── 计价域分组总闸（布尔，sys_config 存 JSON true/false，缺省 fail-open 视为启用）──
     * 与行级 status 区分：这是整组总闸，关闭后 H5 拉取不到该组任何项，但不改动 pricing_rule 各行 status。
     * 命名沿用「领域点号 + 蛇形」，组名用后端单数 group_code（tier/duration/addon/pattern/refund）。 */
    /** 架次档位组开关 */
    public static final String PRICING_TIER_ENABLED = "pricing.tier_enabled";
    /** 表演时长组开关 */
    public static final String PRICING_DURATION_ENABLED = "pricing.duration_enabled";
    /** 附加服务组开关 */
    public static final String PRICING_ADDON_ENABLED = "pricing.addon_enabled";
    /** 预设图案组开关 */
    public static final String PRICING_PATTERN_ENABLED = "pricing.pattern_enabled";
    /** 退款阶梯组开关 */
    public static final String PRICING_REFUND_ENABLED = "pricing.refund_enabled";

    /* ── 支付域 ── */
    /** 下单后支付时限（分钟），超时未付自动取消 */
    public static final String PAY_EXPIRE_MINUTES = "pay_expire_minutes";

    /* ── 档期域全局默认 ── */
    /** 全局默认可接单时段（JSON 数组，元素形如 "19:00-20:00"） */
    public static final String SCHEDULE_DEFAULT_SLOTS = "schedule.default_slots";
    /** 预约窗口（天）：用户最多可预约未来 N 天 */
    public static final String SCHEDULE_BOOKING_WINDOW = "schedule.booking_window_days";
}
