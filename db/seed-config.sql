-- ============================================================
-- 无人机派单系统 · 系统配置种子数据（sys_config，key-value / JSON 值）
-- 库：drone_dispatch
--
-- 承载不适合进领域表的全局标量 / 数组配置，按领域用前缀分组：
--   pricing.* 计价域标量（pricing_rule 表之外的两个定制收费）
--   pay_*      支付域（支付时限，支付模块补）
--   schedule.* 档期域全局默认（默认时段、预约窗口，档期模块补）
--
-- 可重复执行：靠 uk_config_key 用 INSERT IGNORE 去重，不覆盖人工修改。
-- JSON 列用 CAST('...' AS JSON) 写入，数字即 JSON number、数组即 JSON array。
--
-- 前置：先执行 schema.sql 建好 sys_config。
-- 执行（app 账号，仅 DML）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/seed-config.sql
-- ============================================================

SET NAMES utf8mb4;

-- ── 计价域：两个定制收费标量（元）──
INSERT IGNORE INTO sys_config (config_key, config_value, remark) VALUES
    ('pricing.custom_text_price', CAST('2000' AS JSON), '空中文字定制加价（元，0-100000）'),
    ('pricing.custom_logo_price', CAST('5000' AS JSON), 'LOGO / 图片定制加价（元，0-100000）');

-- ── 计价域：五个分组总闸（JSON 布尔，缺省 fail-open 视为启用；关组只影响 H5 可选，不改 pricing_rule 行 status）──
INSERT IGNORE INTO sys_config (config_key, config_value, remark) VALUES
    ('pricing.tier_enabled',     CAST('true' AS JSON), '架次档位组总闸（false 时 H5 拉不到该组）'),
    ('pricing.duration_enabled', CAST('true' AS JSON), '表演时长组总闸（false 时 H5 拉不到该组）'),
    ('pricing.addon_enabled',    CAST('true' AS JSON), '附加服务组总闸（false 时 H5 拉不到该组）'),
    ('pricing.pattern_enabled',  CAST('true' AS JSON), '预设图案组总闸（false 时 H5 拉不到该组）'),
    ('pricing.refund_enabled',   CAST('true' AS JSON), '退款阶梯组总闸（false 时 H5 拉不到该组）');

-- ── 支付域：下单后支付时限（分钟），超时未付自动取消 ──
INSERT IGNORE INTO sys_config (config_key, config_value, remark) VALUES
    ('pay_expire_minutes', CAST('15' AS JSON), '支付时限（分钟，1-120），订单 expire_at 据此计算');

-- ── 档期域：全局默认接单时段（JSON 数组）与预约窗口（天）──
INSERT IGNORE INTO sys_config (config_key, config_value, remark) VALUES
    ('schedule.default_slots', CAST('["19:00-20:00","20:00-21:00","21:00-22:00"]' AS JSON), '全局默认可接单时段（整点区间，单日未单独配置时沿用）'),
    ('schedule.booking_window_days', CAST('60' AS JSON), '预约窗口（天，7-365），用户最多可预约未来 N 天');

-- 核对
SELECT config_key, config_value FROM sys_config ORDER BY config_key;
