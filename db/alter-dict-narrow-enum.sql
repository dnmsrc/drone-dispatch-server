-- ============================================================
-- 无人机派单系统 · 在线库迁移：字典收窄为纯枚举，清理带价类型与死字典 time_slot
-- 库：drone_dispatch
--
-- 背景（配置四域重构，对齐原型 v7）：
--   带价格 / 影响下单金额的 5 类规则已先行迁入 pricing_rule（见 alter-pricing-rule.sql，
--   数据无损可回溯），字典不再承担计价职责，只保留系统级纯枚举。本脚本把这些类型
--   从字典表移除；time_slot（演出时段）早已被档期自由配置取代、无任何逻辑消费，一并清理。
--
-- 处理范围（6 个类型，共 25 项）：
--   带价 5 类：drone_tier / duration / addon / pattern / refund_rule
--   死字典  ：time_slot
-- 保留：order_status（系统级枚举，9 项）。
--
-- 顺序约束：sys_dict_item 有外键 fk_item_dict 指向 sys_dict.dict_code，
--   必须先删子项、再删类型；同一条语句内先 DELETE item 再 DELETE dict。
--
-- 幂等：DELETE 对已不存在的行影响 0 行，不报错，可重复执行。
--   数据已在 pricing_rule 留存，本脚本只清理字典侧副本，不影响计价。
--
-- 执行（app 账号对本库有 ALL PRIVILEGES，可 DML）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/alter-dict-narrow-enum.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1) 先删字典子项，解除外键 fk_item_dict 约束
DELETE FROM sys_dict_item
 WHERE dict_code IN ('drone_tier', 'duration', 'addon', 'pattern', 'refund_rule', 'time_slot');

-- 2) 再删字典类型
DELETE FROM sys_dict
 WHERE dict_code IN ('drone_tier', 'duration', 'addon', 'pattern', 'refund_rule', 'time_slot');

-- 3) 核对：sys_dict 应只剩 order_status；sys_dict_item 应只剩 9 条订单状态
SELECT id, dict_code, dict_name, system_flag, sort FROM sys_dict ORDER BY sort, id;
SELECT dict_code, COUNT(*) AS item_cnt FROM sys_dict_item GROUP BY dict_code;
