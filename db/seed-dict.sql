-- ============================================================
-- 无人机派单系统 · 字典种子数据（7 类字典）
-- 库：drone_dispatch
--
-- 可重复执行：sys_dict 靠 uk_dict_code、sys_dict_item 靠 uk(dict_code,item_key)
--   用 INSERT IGNORE 去重，重复跑不报错、不覆盖人工修改。
-- 数据逐字对齐前端 drone-dispatch-web/src/views/system/dict-mock.ts 的 buildSeed()：
--   6 个内置类型（system_flag=1，计价引擎引用）+ 1 个非内置 order_status（system_flag=0）。
-- item_value 口径：价格类填元、退款比例填 0~1 的四位小数、纯文案/不加价项统一填 0。
--
-- 前置：先执行 schema.sql 建好 sys_dict / sys_dict_item 两表。
-- 执行（应用业务账号 app 即可，仅 DML）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/seed-dict.sql
-- ============================================================

SET NAMES utf8mb4;

-- ───────────────────────── 字典类型（7 行）─────────────────────────

-- sort 决定类型在管理端列表中的顺序（升序）；内置 7 类按 1-7 固定，新增自定义类型由服务端取 max(sort)+1 排到末尾
INSERT IGNORE INTO sys_dict (dict_code, dict_name, system_flag, remark, sort) VALUES
    ('drone_tier',   '架次档位', 1, '下单可选的规格档位；值=基础价（元），说明=适用场景', 1),
    ('duration',     '表演时长', 1, '可选服务时长；值=在基础价上的加价（元），0 表示含在基础价内', 2),
    ('pattern',      '预设图案', 1, 'H5 下单页可选的预设图案（任选，不加价；本业务特有选项）', 3),
    ('addon',        '附加服务', 1, '下单可叠加的增值服务；值=加价（元）', 4),
    ('time_slot',    '演出时段', 1, '每日可预约的服务开始时间（HH:mm）', 5),
    ('refund_rule',  '退款阶梯', 1, '键=距服务小时数下限，值=退款比例（0-1）；按小时数从高到低匹配第一条', 6),
    ('order_status', '订单状态', 0, '订单生命周期状态枚举（通用字典用法示例，可按业务扩展）', 7);

-- ─────────────────────── 架次档位 drone_tier ───────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('drone_tier', '100',  '100 架',   18800.0000, '适合小型告白、生日惊喜', 1, 1),
    ('drone_tier', '200',  '200 架',   28800.0000, '适合婚礼、周年庆典',     2, 1),
    ('drone_tier', '300',  '300 架',   38800.0000, '适合大型求婚、品牌发布', 3, 1),
    ('drone_tier', '500',  '500 架',   58800.0000, '适合企业年会、城市活动', 4, 1),
    ('drone_tier', '1000', '1000 架',  98800.0000, '适合大型节庆、城市级表演', 5, 1);

-- ─────────────────────── 表演时长 duration ────────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('duration', '10', '10 分钟（标准）',     0.0000, '', 1, 1),
    ('duration', '15', '15 分钟',          6000.0000, '', 2, 1),
    ('duration', '20', '20 分钟',         12000.0000, '', 3, 1);

-- ─────────────────────── 预设图案 pattern ─────────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('pattern', 'heart',   '爱心',   0.0000, '', 1, 1),
    ('pattern', 'ring',    '钻戒',   0.0000, '', 2, 1),
    ('pattern', 'star',    '星辰',   0.0000, '', 3, 1),
    ('pattern', 'phoenix', '凤凰',   0.0000, '', 4, 1),
    ('pattern', 'rose',    '玫瑰',   0.0000, '', 5, 1),
    ('pattern', 'moon',    '月亮船', 0.0000, '', 6, 1);

-- ─────────────────────── 附加服务 addon ───────────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('addon', 'music',     '专属音乐编曲',          1000.0000, '', 1, 1),
    ('addon', 'narration', '现场旁白解说',          2000.0000, '', 2, 1),
    ('addon', 'aerial',    '双机位航拍记录',        3000.0000, '', 3, 1),
    ('addon', 'urgent',    '加急服务（48h 内安排）', 8000.0000, '', 4, 1);

-- ─────────────────────── 演出时段 time_slot ───────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('time_slot', '19:00-20:00', '19:00-20:00', 0.0000, '', 1, 1),
    ('time_slot', '20:00-21:00', '20:00-21:00', 0.0000, '', 2, 1),
    ('time_slot', '21:00-22:00', '21:00-22:00', 0.0000, '', 3, 1);

-- ─────────────────────── 退款阶梯 refund_rule ─────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('refund_rule', '168', '距表演 7 天以上，退 90%',        0.9000, '', 1, 1),
    ('refund_rule', '72',  '距表演 3-7 天，退 50%',          0.5000, '', 2, 1),
    ('refund_rule', '24',  '距表演 1-3 天，退 20%',          0.2000, '', 3, 1),
    ('refund_rule', '0',   '距表演不足 24 小时，不可退款',   0.0000, '', 4, 1);

-- ─────────────────────── 订单状态 order_status ────────────────────

INSERT IGNORE INTO sys_dict_item (dict_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('order_status', 'PENDING_PAY',   '待支付', 0.0000, '', 1, 1),
    ('order_status', 'PENDING_ACCEPT','待接单', 0.0000, '', 2, 1),
    ('order_status', 'ACCEPTED',      '已接单', 0.0000, '', 3, 1),
    ('order_status', 'IN_SERVICE',    '执行中', 0.0000, '', 4, 1),
    ('order_status', 'COMPLETED',     '已完成', 0.0000, '', 5, 1),
    ('order_status', 'REVIEWED',      '已评价', 0.0000, '', 6, 1),
    ('order_status', 'CANCELLED',     '已取消', 0.0000, '', 7, 1),
    ('order_status', 'REFUNDING',     '退款中', 0.0000, '', 8, 1),
    ('order_status', 'REFUNDED',      '已退款', 0.0000, '', 9, 1);
