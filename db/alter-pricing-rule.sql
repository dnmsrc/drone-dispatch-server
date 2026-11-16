-- ============================================================
-- 无人机派单系统 · 在线库迁移：计价规则从字典独立为 pricing_rule
-- 库：drone_dispatch
--
-- 背景：带价格的规则（架次/时长/附加/图案/退款）原借 sys_dict/sys_dict_item
--   维护，语义上属于「计价域」而非系统枚举。原型 v7 已按领域拆分，正式后端
--   据此把带价规则独立到 pricing_rule 单表（group_code 分组 + 通用 key/value/extra 列）。
--
-- 本脚本只做「建表 + 无损复制」，不删除字典数据：字典的收窄与 time_slot 清理
--   在后续迁移脚本处理，保证每一步都可独立执行、独立验证、独立回退。
--
-- 幂等：
--   - CREATE TABLE IF NOT EXISTS 重复执行不报错；
--   - INSERT IGNORE 靠唯一键 uk_pricing_group_key 去重，已迁过的行不重复插入，
--     也不覆盖人工在计价表上做的修改。
--
-- 分组映射（dict_code → group_code）：
--   drone_tier→tier  duration→duration  addon→addon  pattern→pattern  refund_rule→refund
--   （time_slot 是已被档期自由配置取代的死字典，不迁入计价表，后续直接清理）
--
-- 执行（app 账号对本库有 ALL PRIVILEGES，可 DDL）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/alter-pricing-rule.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1) 建表（与 schema.sql 的 pricing_rule 定义保持一致）
CREATE TABLE IF NOT EXISTS pricing_rule (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    group_code VARCHAR(24)   NOT NULL COMMENT '规则分组：tier/duration/addon/pattern/refund',
    item_key   VARCHAR(32)   NOT NULL COMMENT '组内程序用 key：架次数/分钟数/英文 key/退款小时下限',
    item_label VARCHAR(64)   NOT NULL COMMENT '显示文案，如「100 架」「专属音乐编曲」',
    item_value DECIMAL(14,4) NOT NULL DEFAULT 0 COMMENT '数值：价格/加价/退款比例(0-1)；不加价项为 0',
    extra      VARCHAR(255)  NULL COMMENT '附加说明：架次适用场景等',
    sort       INT           NOT NULL DEFAULT 0 COMMENT '组内排序号，升序',
    status     TINYINT       NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用；停用后用户端拉取不到',
    created_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_pricing_group_key (group_code, item_key),
    KEY idx_pricing_group_sort (group_code, status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计价规则（带价规则，按分组维护）';

-- 2) 从字典无损复制 5 类带价规则（item_value 为 NULL 时归零，对齐计价表 NOT NULL 口径）
INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status)
SELECT 'tier', item_key, item_label, COALESCE(item_value, 0), extra, sort, status
  FROM sys_dict_item WHERE dict_code = 'drone_tier';

INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status)
SELECT 'duration', item_key, item_label, COALESCE(item_value, 0), extra, sort, status
  FROM sys_dict_item WHERE dict_code = 'duration';

INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status)
SELECT 'addon', item_key, item_label, COALESCE(item_value, 0), extra, sort, status
  FROM sys_dict_item WHERE dict_code = 'addon';

INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status)
SELECT 'pattern', item_key, item_label, COALESCE(item_value, 0), extra, sort, status
  FROM sys_dict_item WHERE dict_code = 'pattern';

INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status)
SELECT 'refund', item_key, item_label, COALESCE(item_value, 0), extra, sort, status
  FROM sys_dict_item WHERE dict_code = 'refund_rule';

-- 3) 核对结果：应见 5 个分组、共 22 行（tier5 + duration3 + addon4 + pattern6 + refund4）
SELECT group_code, COUNT(*) AS cnt, SUM(status) AS enabled_cnt
  FROM pricing_rule GROUP BY group_code ORDER BY group_code;
