-- ============================================================
-- 无人机派单系统 · 全量 DDL（阶段 1）
-- 库：drone_dispatch    MySQL 8.4    引擎 InnoDB    字符集 utf8mb4
-- 约定：
--   1. 主键 BIGINT 自增；业务编号另设唯一键（如 order_no）
--   2. 金额 DECIMAL(12,2)，Java 侧用 BigDecimal，禁用 float/double
--   3. 状态存字符串枚举（VARCHAR），不存魔法数字
--   4. 业务表软删除：deleted=1 + deleted_at；字典/配置类物理删除
--   5. 时间统一 DATETIME(3)（毫秒精度）
-- 执行（DDL 用管理员账号 root，应用运行时才用业务账号 app）：
--   mysql -uroot -p drone_dispatch < schema.sql
-- 可重复执行：IF NOT EXISTS，不会清数据
-- ============================================================

SET NAMES utf8mb4;

-- ───────────────────────── 账号域（2 表）─────────────────────────

-- 1. C 端用户
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid      VARCHAR(64)  NULL COMMENT '微信 openid，演示阶段可空',
    nickname    VARCHAR(64)  NOT NULL COMMENT '昵称',
    avatar      VARCHAR(255) NULL COMMENT '头像 URL',
    phone       VARCHAR(20)  NULL COMMENT '手机号（真实系统应加密存储）',
    gender      TINYINT      NOT NULL DEFAULT 0 COMMENT '0 未知 / 1 男 / 2 女',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除：0 正常 1 已删',
    deleted_at  DATETIME(3)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_openid (openid),
    KEY idx_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='C 端用户';

-- 2. 管理端账号
CREATE TABLE IF NOT EXISTS admin_users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(32)  NOT NULL COMMENT '登录名 admin/operator',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希，禁止存明文',
    real_name     VARCHAR(32)  NULL COMMENT '显示名',
    role          VARCHAR(16)  NOT NULL COMMENT 'ADMIN 管理员 / OPERATOR 操作员',
    enabled       TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
    last_login_at DATETIME(3)  NULL,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理端账号';

-- ─────────────────────── 基础配置域（5 表）───────────────────────

-- 3. 字典类型（7 类：drone_tier/duration/pattern/addon/time_slot/refund_rule/order_status）
CREATE TABLE IF NOT EXISTS sys_dict (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    dict_code   VARCHAR(32) NOT NULL COMMENT '字典编码，程序里引用它',
    dict_name   VARCHAR(32) NOT NULL COMMENT '显示名，如「架次档位」',
    system_flag TINYINT     NOT NULL DEFAULT 1 COMMENT '内置字典禁止删除',
    remark      VARCHAR(255) NULL,
    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型';

-- 4. 字典项
CREATE TABLE IF NOT EXISTS sys_dict_item (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    dict_code  VARCHAR(32)   NOT NULL COMMENT '所属字典',
    item_key   VARCHAR(32)   NOT NULL COMMENT '程序用 key，如 100 / heart / WECHAT',
    item_label VARCHAR(64)   NOT NULL COMMENT '显示文案，如「100 架」',
    item_value DECIMAL(14,4) NULL COMMENT '数值：价格 18800.0000 / 退款比例 0.9000；纯文案项为 NULL',
    extra      VARCHAR(255)  NULL COMMENT '附加说明',
    sort       INT           NOT NULL DEFAULT 0,
    status     TINYINT       NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
    created_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_item (dict_code, item_key),
    CONSTRAINT fk_item_dict FOREIGN KEY (dict_code) REFERENCES sys_dict (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典项';

-- 5. 系统配置（key-value，值统一 JSON 类型）
CREATE TABLE IF NOT EXISTS sys_config (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    config_key   VARCHAR(64) NOT NULL COMMENT 'daily_limit / pay_expire_minutes ...',
    config_value JSON        NOT NULL COMMENT '数字、数组都以 JSON 存放',
    remark       VARCHAR(255) NULL,
    created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

-- 6. 预设场地
CREATE TABLE IF NOT EXISTS venues (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100)  NOT NULL,
    address    VARCHAR(200)  NOT NULL,
    lng        DECIMAL(10,6) NULL COMMENT '经度',
    lat        DECIMAL(10,6) NULL COMMENT '纬度',
    enabled    TINYINT       NOT NULL DEFAULT 1,
    created_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted    TINYINT       NOT NULL DEFAULT 0,
    deleted_at DATETIME(3)   NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预设场地';

-- 7. 档期日配置（一天一行）
CREATE TABLE IF NOT EXISTS schedule_days (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    schedule_date DATE      NOT NULL,
    daily_limit INT         NULL COMMENT '当日上限，NULL = 沿用全局配置',
    closed      TINYINT     NOT NULL DEFAULT 0 COMMENT '休整日标记',
    slots       JSON        NULL COMMENT '当日时段数组，NULL = 用默认时段',
    note        VARCHAR(100) NULL COMMENT '休整/节日说明',
    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_date (schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='档期日配置';

-- ──────────────────────── 订单域（7 表）──────────────────────────

-- 8. 订单主表
CREATE TABLE IF NOT EXISTS orders (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    order_no             VARCHAR(32)   NOT NULL COMMENT '业务编号 D20261104-0023',
    user_id              BIGINT        NOT NULL,
    status               VARCHAR(32)   NOT NULL DEFAULT 'PENDING_PAY'
        COMMENT 'PENDING_PAY/PARTIALLY_PAID/PENDING_ACCEPT/ACCEPTED/IN_SERVICE/COMPLETED/REVIEWED/CANCELLED/REFUNDING/REFUNDED（10 态，PARTIALLY_PAID=预付款已到、尾款未付）',
    drone_count          INT           NOT NULL COMMENT '架次 100/200/300/500/1000',
    duration_minutes     INT           NOT NULL COMMENT '表演时长（分钟）',
    pattern_preset       JSON          NULL COMMENT '预设图案 key 数组，如 ["heart"]',
    custom_text          VARCHAR(100)  NULL COMMENT '定制文字',
    custom_logo          VARCHAR(255)  NULL COMMENT '上传图文件名/URL',
    addons               JSON          NULL COMMENT '附加服务 key 数组',
    occasion             VARCHAR(50)   NULL COMMENT '场合',
    perform_date         DATE          NOT NULL,
    time_slot            VARCHAR(20)   NOT NULL COMMENT '如 19:00-20:00',
    venue_id             BIGINT        NULL,
    venue_name           VARCHAR(100)  NULL COMMENT '场地快照：下单时的名称',
    venue_address        VARCHAR(200)  NULL COMMENT '场地快照：地址',
    venue_lng            DECIMAL(10,6) NULL,
    venue_lat            DECIMAL(10,6) NULL,
    contact_name         VARCHAR(50)   NOT NULL COMMENT '联系人（小对象摊平）',
    contact_phone        VARCHAR(20)   NOT NULL,
    total_amount         DECIMAL(12,2) NOT NULL COMMENT '服务端计价结果',
    pay_channel          VARCHAR(16)   NULL COMMENT 'WECHAT / ALIPAY',
    pay_trade_no         VARCHAR(64)   NULL COMMENT '成功支付流水号（冗余展示，真身在 payments）',
    paid_at              DATETIME(3)   NULL,
    accepted_by_admin_id BIGINT        NULL,
    accepted_at          DATETIME(3)   NULL,
    started_at           DATETIME(3)   NULL,
    completed_at         DATETIME(3)   NULL,
    cancel_reason        VARCHAR(255)  NULL,
    expire_at            DATETIME(3)   NOT NULL COMMENT '支付截止 = 创建时间 + 15 分钟',
    created_at           DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at           DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    deleted              TINYINT       NOT NULL DEFAULT 0,
    deleted_at           DATETIME(3)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_orders_user (user_id),
    KEY idx_orders_status (status),
    KEY idx_orders_perform (perform_date, time_slot),
    CONSTRAINT fk_orders_user      FOREIGN KEY (user_id)              REFERENCES users (id),
    CONSTRAINT fk_orders_venue     FOREIGN KEY (venue_id)             REFERENCES venues (id),
    CONSTRAINT fk_orders_admin     FOREIGN KEY (accepted_by_admin_id) REFERENCES admin_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- 9. 订单价格明细（原型 priceDetail 数组）
CREATE TABLE IF NOT EXISTS order_price_items (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    order_id   BIGINT        NOT NULL,
    label      VARCHAR(100)  NOT NULL COMMENT '如「300 架基础价（含 10 分钟）」',
    amount     DECIMAL(12,2) NOT NULL,
    sort       INT           NOT NULL DEFAULT 0,
    created_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_price_order (order_id),
    CONSTRAINT fk_price_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单价格明细';

-- 10. 订单状态事件（审计 + 前端时间线，每次状态变更一条）
CREATE TABLE IF NOT EXISTS order_events (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    order_id    BIGINT      NOT NULL,
    event_type  VARCHAR(32) NOT NULL COMMENT 'CREATE/PAY/ACCEPT/REJECT/START/COMPLETE/REFUND_APPLY ...',
    from_status VARCHAR(32) NULL,
    to_status   VARCHAR(32) NULL,
    actor_type  VARCHAR(16) NOT NULL COMMENT 'USER / ADMIN / SYSTEM',
    actor_id    BIGINT      NULL COMMENT '多态引用，不建物理外键',
    note        VARCHAR(255) NULL COMMENT '时间线展示文案',
    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_events_order (order_id, id),
    CONSTRAINT fk_events_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态事件';

-- 11. 支付流水（独立于订单，一单可多次支付尝试；同一期失败重试会产生多行，分期由 schedule_id 区分）
CREATE TABLE IF NOT EXISTS payments (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    order_id            BIGINT        NOT NULL,
    schedule_id         BIGINT        NULL COMMENT '合同付款计划期次 → contract_payment_schedule.id（逻辑外键，应用层保证；一次性支付为空）',
    pay_method          VARCHAR(16)   NOT NULL DEFAULT 'ONLINE' COMMENT 'ONLINE 在线支付 / OFFLINE 对公转账（人工确认到账）',
    channel             VARCHAR(16)   NOT NULL COMMENT 'ONLINE：WECHAT/ALIPAY；OFFLINE 时填 OFFLINE',
    trade_no            VARCHAR(64)   NULL COMMENT '渠道流水号，发起时可空；对公转账填银行回单号',
    amount              DECIMAL(12,2) NOT NULL,
    status              VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED/CLOSED',
    fail_reason         VARCHAR(255)  NULL,
    voucher_url         VARCHAR(255)  NULL COMMENT '线下付款凭证/银行回单图片地址，对公转账必填',
    confirm_by_admin_id BIGINT        NULL COMMENT '线下到账确认员工 → admin_users.id（在线支付时为空）',
    confirmed_at        DATETIME(3)   NULL COMMENT '人工确认到账时间',
    paid_at             DATETIME(3)   NULL,
    created_at          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_pay_order (order_id),
    KEY idx_pay_schedule (schedule_id),
    KEY idx_pay_trade_no (trade_no),
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_pay_confirm_admin FOREIGN KEY (confirm_by_admin_id) REFERENCES admin_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付流水';

-- 12. 退款申请与审批（驳回后可再次申请，一对多）
CREATE TABLE IF NOT EXISTS refunds (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    order_id          BIGINT        NOT NULL,
    refund_no         VARCHAR(32)   NOT NULL,
    reason            VARCHAR(255)  NOT NULL COMMENT '用户必填原因',
    ratio             DECIMAL(5,4)  NOT NULL COMMENT '试算比例 1.0000/0.9000/0.5000/0.2000',
    apply_amount      DECIMAL(12,2) NOT NULL,
    refund_amount     DECIMAL(12,2) NULL COMMENT '审批时可改的实退额',
    status            VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    prev_status       VARCHAR(32)   NOT NULL COMMENT '申请前订单状态，驳回回退用',
    audit_by_admin_id BIGINT        NULL,
    audit_note        VARCHAR(255)  NULL,
    audited_at        DATETIME(3)   NULL,
    created_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_refund_no (refund_no),
    KEY idx_refund_order (order_id),
    CONSTRAINT fk_refund_order FOREIGN KEY (order_id)          REFERENCES orders (id),
    CONSTRAINT fk_refund_admin FOREIGN KEY (audit_by_admin_id) REFERENCES admin_users (id),
    CONSTRAINT chk_refund_ratio CHECK (ratio BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款申请与审批';

-- 13. 评价（一单一评）
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    order_id   BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    stars      TINYINT     NOT NULL,
    content    VARCHAR(500) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_review_order (order_id),
    KEY idx_review_user (user_id),
    CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_review_user  FOREIGN KEY (user_id)  REFERENCES users (id),
    CONSTRAINT chk_review_stars CHECK (stars BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单评价';

-- 14. 回传照片/视频
CREATE TABLE IF NOT EXISTS order_media (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    order_id   BIGINT      NOT NULL,
    media_type VARCHAR(8)  NOT NULL COMMENT 'PHOTO / VIDEO',
    url        VARCHAR(255) NOT NULL,
    sort       INT         NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_media_order (order_id),
    CONSTRAINT fk_media_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单回传媒体';

-- ──────────────────────── 合同与分期域（3 表）────────────────────
-- 背景：大额定制服务按行业惯例采用「里程碑分期、客户主动付款」，不做自动代扣
--   （微信委托代扣仅开放周期扣款/先享后付白名单场景，本业务不在其列）。
-- 一期电子签不对接第三方平台：合同表预留 esign_* 字段，过渡期由管理员上传已签 PDF。

-- 15. 电子合同（与订单 1:1，订单创建时同步生成草稿）
CREATE TABLE IF NOT EXISTS contracts (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    contract_no        VARCHAR(32)  NOT NULL COMMENT '合同编号，如 HT20261108-0007',
    order_id           BIGINT       NOT NULL COMMENT '关联订单（唯一约束保证一单一合同）',
    template_code      VARCHAR(32)  NOT NULL COMMENT '合同模板编码',
    template_version   VARCHAR(16)  NOT NULL COMMENT '模板版本号：模板改版不影响已签老合同（版本快照）',
    party_a_name       VARCHAR(64)  NOT NULL COMMENT '甲方（客户）名称快照',
    party_a_id_type    VARCHAR(16)  NULL COMMENT '甲方证件类型：ID_CARD 身份证 / UNIFORM_CODE 统一社会信用代码',
    party_a_id_no      VARCHAR(32)  NULL COMMENT '甲方证件号（真实系统应加密存储）',
    party_a_contact    VARCHAR(50)  NULL COMMENT '甲方联系人',
    party_a_phone      VARCHAR(20)  NULL COMMENT '甲方联系电话',
    party_b_name       VARCHAR(100) NOT NULL COMMENT '乙方（本公司）名称快照',
    party_b_credit_code VARCHAR(32) NULL COMMENT '乙方统一社会信用代码快照',
    total_amount       DECIMAL(12,2) NOT NULL COMMENT '合同总金额（与订单总价一致的快照）',
    payment_terms      VARCHAR(500) NOT NULL COMMENT '付款条款文字快照，如「签约付50%，演出前7天付清尾款」',
    sign_status        VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT 草稿/PENDING_SIGN 待签/SIGNED 已签/VOID 作废',
    esign_flow_id      VARCHAR(64)  NULL COMMENT '电子签平台签署流程号（一期预留，暂不对接）',
    seal_id            VARCHAR(64)  NULL COMMENT '电子签平台印章标识（章托管在平台，本地不建印章表）',
    signed_pdf_url     VARCHAR(255) NULL COMMENT '已签署合同 PDF 存档地址（过渡期由管理员上传）',
    party_a_signed_at  DATETIME(3)  NULL COMMENT '甲方签署时间',
    party_b_signed_at  DATETIME(3)  NULL COMMENT '乙方盖章时间',
    signed_at          DATETIME(3)  NULL COMMENT '合同整体生效时间（双方都签完）',
    void_reason        VARCHAR(255) NULL COMMENT '作废原因',
    created_at         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_contract_no (contract_no),
    UNIQUE KEY uk_contract_order (order_id),
    CONSTRAINT fk_contract_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='电子合同';

-- 16. 合同付款计划（一份合同 N 期：默认两期 50%+50%，表结构支持任意期数）
CREATE TABLE IF NOT EXISTS contract_payment_schedule (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    contract_id    BIGINT        NOT NULL COMMENT '属于哪份合同 → contracts.id',
    installment_no INT           NOT NULL COMMENT '期次，从 1 开始（1=预付款 2=尾款）',
    stage_name     VARCHAR(50)   NOT NULL COMMENT '期次名称，如「预付款」「尾款」',
    trigger_event  VARCHAR(100)  NOT NULL COMMENT '触发节点，如「合同签署完成」「演出前7天」「验收通过」',
    due_date       DATE          NULL COMMENT '应付日期，可空（按触发事件动态计算时）',
    ratio          DECIMAL(5,4)  NOT NULL COMMENT '本期占合同总额比例，如 0.5000',
    plan_amount    DECIMAL(12,2) NOT NULL COMMENT '本期应付金额',
    paid_amount    DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '本期累计实付金额',
    status         VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING 待付/PARTIAL 部分已付/PAID 已付/OVERDUE 逾期',
    paid_at        DATETIME(3)   NULL COMMENT '本期结清时间',
    created_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_contract_installment (contract_id, installment_no),
    CONSTRAINT fk_schedule_contract FOREIGN KEY (contract_id) REFERENCES contracts (id),
    CONSTRAINT chk_schedule_ratio CHECK (ratio BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='合同付款计划';

-- 17. 公司收款账户（对公转账到账核对用，独立基础数据表）
CREATE TABLE IF NOT EXISTS company_bank_accounts (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    account_name  VARCHAR(100) NOT NULL COMMENT '开户户名（公司全称）',
    bank_name     VARCHAR(100) NOT NULL COMMENT '开户行，如「中国工商银行杭州滨江支行」',
    account_no    VARCHAR(40)  NOT NULL COMMENT '银行账号',
    is_default    TINYINT      NOT NULL DEFAULT 0 COMMENT '1 默认收款账户 0 非默认',
    enabled       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    remark        VARCHAR(255) NULL,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司收款账户';
