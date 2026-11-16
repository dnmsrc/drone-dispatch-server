-- ============================================================
-- 无人机派单系统 · 在线库迁移：废弃 schedule_days.daily_limit
-- 库：drone_dispatch
--
-- 背景（配置四域重构，对齐原型 v7）：档期可接单名额不再用独立的 daily_limit
--   字段配置，统一由「时段数」派生——一个时段 = 一个名额，当日 limit = slots 长度，
--   已订满的时段在服务端计算 openSlots 时过滤。daily_limit 与时段数会产生两个
--   互相矛盾的容量口径，故移除该列。
--
-- 幂等：MySQL 8.4 的 ALTER TABLE 不支持 DROP COLUMN IF EXISTS，先查
--   information_schema 判断列是否存在，再用预处理语句决定是否执行 DDL；
--   重复执行只会提示已跳过，不会报错。
--
-- 执行（app 账号对本库有 ALL PRIVILEGES，可 DDL）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/alter-schedule-drop-daily-limit.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1) 幂等删列：schedule_days.daily_limit
SET @col_exists := (
    SELECT COUNT(*)
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'schedule_days'
       AND COLUMN_NAME = 'daily_limit'
);
SET @ddl := IF(
    @col_exists > 0,
    'ALTER TABLE schedule_days DROP COLUMN daily_limit',
    'SELECT ''schedule_days.daily_limit already dropped, skipped'' AS msg'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 核对结果（应不再出现 daily_limit）
SHOW COLUMNS FROM schedule_days;
