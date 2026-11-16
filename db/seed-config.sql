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

-- ── 支付域：下单后支付时限（分钟），超时未付自动取消 ──
INSERT IGNORE INTO sys_config (config_key, config_value, remark) VALUES
    ('pay_expire_minutes', CAST('15' AS JSON), '支付时限（分钟，1-120），订单 expire_at 据此计算');

-- 核对
SELECT config_key, config_value FROM sys_config ORDER BY config_key;
