-- =============================================================
-- AI Agent 模块建表脚本
-- 适用数据库：MySQL 8.x（与主库 dbproject 同库）
-- 执行方式：mysql -u root -p dbproject < scripts/ai_tables.sql
-- =============================================================

-- 删除顺序：先子表后父表
DROP TABLE IF EXISTS ai_messages;
DROP TABLE IF EXISTS ai_conversations;
DROP TABLE IF EXISTS ai_sql_exec_log;
DROP TABLE IF EXISTS ai_sql_feedback;
DROP TABLE IF EXISTS ai_sql_memory;

-- =============================================================
-- 1. ai_sql_memory — SQL 模板记忆表
--    存储 LLM 生成的参数化 SQL 模板，供后续检索复用。
-- =============================================================
CREATE TABLE ai_sql_memory (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '主键',
    question_text   TEXT            NOT NULL                    COMMENT '用户原始自然语言问题',
    normalized_question VARCHAR(500) NOT NULL                   COMMENT '归一化问题（小写、去停用词，用于检索匹配）',
    sql_template    TEXT            NOT NULL                    COMMENT '参数化 SQL 模板（含 {param} 占位符）',
    params_spec     JSON            NOT NULL                    COMMENT '参数规格 JSON 数组 [{name,type,default,required,label}]',
    tables_used     VARCHAR(500)    DEFAULT ''                  COMMENT '涉及的表名（逗号分隔，便于表重叠度计算）',
    intent_tag      VARCHAR(100)    DEFAULT ''                  COMMENT '意图标签（ranking/trend/proportion/summary/detail）',
    chart_hint      JSON            DEFAULT NULL                COMMENT '图表建议 {type,x,y,series}',
    confidence      DECIMAL(4,3)    DEFAULT 0.000               COMMENT 'LLM 生成时的置信度（0~1）',

    -- 使用统计
    use_count       INT             DEFAULT 0                   COMMENT '被复用次数',
    success_count   INT             DEFAULT 0                   COMMENT '执行成功次数',
    last_used_at    DATETIME        DEFAULT NULL                COMMENT '最近一次被使用的时间',

    -- 版本管理
    version         INT             DEFAULT 1                   COMMENT '模板版本号（每次改写自增）',
    status          ENUM('draft','published','archived')
                                    DEFAULT 'draft'             COMMENT '状态：draft=草稿，published=已发布可检索，archived=已归档',

    -- 审计
    created_by      BIGINT          DEFAULT NULL                COMMENT '创建人（staffs.id）',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_normalized_question (normalized_question(200)),
    INDEX idx_tables_used (tables_used(200)),
    INDEX idx_intent_tag (intent_tag),
    INDEX idx_status (status),
    INDEX idx_last_used_at (last_used_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI SQL 模板记忆表：存储 LLM 生成的参数化 SQL，供历史检索复用';


-- =============================================================
-- 2. ai_sql_feedback — SQL 模板反馈表
--    用户对 SQL 模板的点赞/点踩记录。
-- =============================================================
CREATE TABLE ai_sql_feedback (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT  COMMENT '主键',
    memory_id   BIGINT      NOT NULL                    COMMENT '关联 ai_sql_memory.id',
    user_id     BIGINT      NOT NULL                    COMMENT '反馈用户（staffs.id）',
    vote        TINYINT     NOT NULL                    COMMENT '+1 表示赞，-1 表示踩',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP   COMMENT '反馈时间',

    CONSTRAINT fk_feedback_memory
        FOREIGN KEY (memory_id) REFERENCES ai_sql_memory(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    UNIQUE KEY uk_memory_user (memory_id, user_id),
    INDEX idx_memory_id (memory_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI SQL 反馈表：记录用户对 SQL 模板的点赞/点踩';


-- =============================================================
-- 3. ai_sql_exec_log — SQL 执行日志表
--    每次通过 AI 接口执行 SQL 的详细记录，用于审计与统计。
-- =============================================================
CREATE TABLE ai_sql_exec_log (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT  COMMENT '主键',
    memory_id   BIGINT      DEFAULT NULL                COMMENT '关联 ai_sql_memory.id（首次生成时可为空）',
    user_id     BIGINT      NOT NULL                    COMMENT '执行人（staffs.id）',
    sql_hash    VARCHAR(64) NOT NULL                    COMMENT 'SQL 文本 SHA-256 哈希',
    sql_text    TEXT        NOT NULL                    COMMENT '实际执行的完整 SQL（参数已替换）',
    params      JSON        DEFAULT NULL                COMMENT '实际执行参数',
    row_count   INT         DEFAULT 0                   COMMENT '返回行数',
    latency_ms  INT         DEFAULT 0                   COMMENT '执行耗时（毫秒）',
    status      ENUM('success','explain_fail','security_reject','timeout','error')
                            NOT NULL                    COMMENT '执行状态',
    error_msg   TEXT        DEFAULT NULL                COMMENT '错误信息（仅失败时填写）',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP   COMMENT '执行时间',

    CONSTRAINT fk_exec_log_memory
        FOREIGN KEY (memory_id) REFERENCES ai_sql_memory(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    INDEX idx_memory_id (memory_id),
    INDEX idx_user_id (user_id),
    INDEX idx_sql_hash (sql_hash),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI SQL 执行日志表：记录每次 AI 接口的 SQL 执行详情，用于审计与统计';


-- =============================================================
-- 4. ai_conversations — AI 对话表
--    存储用户的 AI 对话会话。
-- =============================================================
CREATE TABLE ai_conversations (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '主键',
    user_id         BIGINT          NOT NULL                    COMMENT '所属用户（staffs.id）',
    title           VARCHAR(200)    DEFAULT ''                  COMMENT '对话标题',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    is_deleted      TINYINT         DEFAULT 0                   COMMENT '是否软删除:0=正常,1=已删除',
    deleted_at      DATETIME        DEFAULT NULL                COMMENT '删除时间',

    INDEX idx_user_updated (user_id, updated_at DESC),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI conversation table';


-- =============================================================
-- 5. ai_messages — AI 消息表
--    存储对话中的每条消息（用户输入 + AI 回复）。
-- =============================================================
CREATE TABLE ai_messages (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '主键',
    conversation_id BIGINT          NOT NULL                    COMMENT '所属对话 ID',
    role            VARCHAR(20)     NOT NULL                    COMMENT '消息角色：user/assistant',
    content         TEXT            NOT NULL                    COMMENT '消息内容',
    sql_result      JSON            DEFAULT NULL                COMMENT 'AI 返回的 SQL 结果（sqlTemplate/paramsSpec/chartHint 等）',
    error_msg       VARCHAR(500)    DEFAULT NULL                COMMENT '错误信息',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',

    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_conversation_created (conversation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI 消息表：存储对话中的每条消息';


-- =============================================================
-- 说明
-- =============================================================
/*
  表关系：
    ai_conversations  1 ──< N  ai_messages        （一个对话包含多条消息）
    ai_sql_memory     1 ──< N  ai_sql_feedback    （一个模板可被多人反馈，每人限一次）
    ai_sql_memory     1 ──< N  ai_sql_exec_log    （一个模板可被执行多次）

  使用场景：
    Phase1（当前）：仅使用 ai_sql_exec_log 做审计日志。
    Phase2（后续）：启用 ai_sql_memory + ai_sql_feedback 做检索复用与反馈排序。

  索引策略：
    - normalized_question 前缀索引（200字符）用于关键词检索。
    - tables_used 前缀索引用于表重叠度计算。
    - intent_tag 索引用于意图匹配过滤。
    - sql_hash 索引用于去重与快速定位。
    - created_at 索引用于时间范围查询与审计。

  安全说明：
    - ai_sql_exec_log.sql_text 存储的是参数已替换后的完整 SQL，
      如果数据库中有敏感数据，建议对该字段做脱敏或访问控制。
    - feedback 表的 uk_memory_user 唯一键保证每个用户对同一模板只能投一票，
      再次投票会更新（需在业务层用 INSERT ... ON DUPLICATE KEY UPDATE 实现）。
*/
