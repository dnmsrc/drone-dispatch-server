-- ============================================================
-- 无人机派单系统 · 字典种子数据（收窄后：仅系统级纯枚举）
-- 库：drone_dispatch
--
-- 配置四域重构（对齐原型 v7）后的字典定位：
--   字典只放「不带价格、纯文案」的系统级枚举；带价格 / 影响下单金额的规则
--   （架次/时长/附加/图案/退款）已迁至 pricing_rule（见 seed-pricing.sql），
--   演出时段 time_slot 已被档期自由配置取代而停用清理。
--   故本文件当前只剩 order_status 一个内置枚举类型。
--
-- 可重复执行：sys_dict 靠 uk_dict_code、sys_dict_item 靠 uk(dict_code,item_key)
--   用 INSERT IGNORE 去重，重复跑不报错、不覆盖人工修改。
--
-- 前置：先执行 schema.sql 建好 sys_dict / sys_dict_item 两表。
-- 执行（应用业务账号 app 即可，仅 DML）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/seed-dict.sql
-- ============================================================

SET NAMES utf8mb4;

-- ───────────────────────── 字典类型（纯枚举，当前 1 行）─────────────────────────

-- sort 决定类型在管理端列表中的顺序（升序）；新增自定义枚举类型由服务端取 max(sort)+1 排到末尾
INSERT IGNORE INTO sys_dict (dict_code, dict_name, system_flag, remark, sort) VALUES
    ('order_status', '订单状态', 0, '订单生命周期状态枚举（系统级下拉，不带价格；可按业务扩展）', 1);

-- ─────────────────────── 订单状态 order_status ────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('order_status', 'PENDING_PAY',    '待支付', 0.0000, '', 1, 1),
    ('order_status', 'PENDING_ACCEPT', '待接单', 0.0000, '', 2, 1),
    ('order_status', 'ACCEPTED',       '已接单', 0.0000, '', 3, 1),
    ('order_status', 'IN_SERVICE',     '执行中', 0.0000, '', 4, 1),
    ('order_status', 'COMPLETED',      '已完成', 0.0000, '', 5, 1),
    ('order_status', 'REVIEWED',       '已评价', 0.0000, '', 6, 1),
    ('order_status', 'CANCELLED',      '已取消', 0.0000, '', 7, 1),
    ('order_status', 'REFUNDING',      '退款中', 0.0000, '', 8, 1),
    ('order_status', 'REFUNDED',       '已退款', 0.0000, '', 9, 1);
