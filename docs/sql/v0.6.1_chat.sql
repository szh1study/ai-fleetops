-- V0.6.1 AI 助手：会话与消息转录表
-- 在 ai_fleetops 库执行一次。
-- 注意：这里只存给前端回看的文本转录，LLM 多轮上下文由 Spring AI ChatMemory（内存）管理。

CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) COMMENT '会话标题，取首条用户消息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 助手会话';

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL COMMENT '会话 ID',
    role VARCHAR(16) NOT NULL COMMENT 'user / assistant',
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 助手消息转录';
