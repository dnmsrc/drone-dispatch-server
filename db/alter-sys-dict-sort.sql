-- ============================================================
-- 无人机派单系统 · 在线库迁移：sys_dict 增加排序号 sort
-- 库：drone_dispatch
--
-- 背景：字典类型列表原按 dict_code 字母序排列，新增的自定义类型会按字母
--   插入中间，不符合「按新增顺序排列」。新增显式 sort 列，列表改按 sort,id 升序，
--   新增类型缺省 sort = 当前 max(sort)+1，自然排到末尾，同时支持手动调整顺序。
--
-- 幂等：MySQL 8.4 的 ALTER TABLE 不支持 ADD COLUMN IF NOT EXISTS，
--   故先查 information_schema 判断列是否存在，再用预处理语句决定是否执行 DDL；
--   回填仅作用于仍为默认值 0 的行，重复执行不会覆盖人工调整过的排序。
--
-- 执行（app 账号对本库有 ALL PRIVILEGES，可 DDL）：
--   mysql -uapp -p --default-character-set=utf8mb4 drone_dispatch < db/alter-sys-dict-sort.sql
-- ============================================================

SET NAMES utf8mb4;

-- 1) 幂等加列：sys_dict.sort（位于 remark 之后、created_at 之前）
SET @col_exists := (
    SELECT COUNT(*)
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'sys_dict'
       AND COLUMN_NAME = 'sort'
);
SET @ddl := IF(
    @col_exists = 0,
    'ALTER TABLE sys_dict ADD COLUMN sort INT NOT NULL DEFAULT 0 COMMENT ''排序号，升序；缺省按新增顺序（max+1）'' AFTER remark',
    'SELECT ''sys_dict.sort already exists, skipped'' AS msg'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 回填存量行：sort 置为 id（等价按创建先后；id 1-7 内置得 1-7，自定义类型随 id 排到最后）
--    仅回填仍为默认 0 的行，重复执行不影响已人工调整的排序。
UPDATE sys_dict SET sort = id WHERE sort = 0;

-- 3) 核对结果
SELECT id, dict_code, dict_name, system_flag, sort FROM sys_dict ORDER BY sort, id;
