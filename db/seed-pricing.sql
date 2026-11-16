-- ============================================================
-- 无人机派单系统 · 计价规则种子数据（pricing_rule，5 个分组共 22 行）
-- 库：drone_dispatch
--
-- 数据逐字对齐原字典种子 seed-dict.sql 的 5 类带价规则（原型 v7 迁入计价域）：
--   tier 架次档位（值=基础价）/ duration 时长加价 / addon 附加服务 /
--   pattern 预设图案（0 元可选项）/ refund 退款阶梯（值=0~1 退款比例）。
--   定制文字价、LOGO 价是标量，走 sys_config，不在此表。
--
-- 可重复执行：靠唯一键 uk_pricing_group_key 用 INSERT IGNORE 去重，
--   重复跑不报错、不覆盖人工修改。
--
-- 前置：先执行 schema.sql（含 pricing_rule 建表）。
-- 执行（app 账号，仅 DML）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/seed-pricing.sql
-- ============================================================

SET NAMES utf8mb4;

-- ─────────────────────── 架次档位 tier（值=基础价，extra=适用场景）───────────────────────
INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('tier', '100',  '100 架',  18800.0000, '适合小型告白、生日惊喜',       1, 1),
    ('tier', '200',  '200 架',  28800.0000, '适合婚礼、周年庆典',           2, 1),
    ('tier', '300',  '300 架',  38800.0000, '适合大型求婚、品牌发布',       3, 1),
    ('tier', '500',  '500 架',  58800.0000, '适合企业年会、城市活动',       4, 1),
    ('tier', '1000', '1000 架', 98800.0000, '适合大型节庆、城市级表演',     5, 1);

-- ─────────────────────── 表演时长 duration（值=在基础价上的加价，0=含基础价）───────────────────────
INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('duration', '10', '10 分钟（标准）',     0.0000, '', 1, 1),
    ('duration', '15', '15 分钟',          6000.0000, '', 2, 1),
    ('duration', '20', '20 分钟',         12000.0000, '', 3, 1);

-- ─────────────────────── 附加服务 addon（值=加价）───────────────────────
INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('addon', 'music',     '专属音乐编曲',          1000.0000, '', 1, 1),
    ('addon', 'narration', '现场旁白解说',          2000.0000, '', 2, 1),
    ('addon', 'aerial',    '双机位航拍记录',        3000.0000, '', 3, 1),
    ('addon', 'urgent',    '加急服务（48h 内安排）', 8000.0000, '', 4, 1);

-- ─────────────────────── 预设图案 pattern（任选不加价，归计价域统一维护）───────────────────────
INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('pattern', 'heart',   '爱心',   0.0000, '', 1, 1),
    ('pattern', 'ring',    '钻戒',   0.0000, '', 2, 1),
    ('pattern', 'star',    '星辰',   0.0000, '', 3, 1),
    ('pattern', 'phoenix', '凤凰',   0.0000, '', 4, 1),
    ('pattern', 'rose',    '玫瑰',   0.0000, '', 5, 1),
    ('pattern', 'moon',    '月亮船', 0.0000, '', 6, 1);

-- ─────────────────────── 退款阶梯 refund（键=距服务小时下限，值=退款比例，按小时降序匹配第一条）───────────────────────
INSERT IGNORE INTO pricing_rule (group_code, item_key, item_label, item_value, extra, sort, status) VALUES
    ('refund', '168', '距表演 7 天以上，退 90%',        0.9000, '', 1, 1),
    ('refund', '72',  '距表演 3-7 天，退 50%',          0.5000, '', 2, 1),
    ('refund', '24',  '距表演 1-3 天，退 20%',          0.2000, '', 3, 1),
    ('refund', '0',   '距表演不足 24 小时，不可退款',   0.0000, '', 4, 1);

-- 核对：5 个分组，共 22 行
SELECT group_code, COUNT(*) AS cnt FROM pricing_rule GROUP BY group_code ORDER BY group_code;
