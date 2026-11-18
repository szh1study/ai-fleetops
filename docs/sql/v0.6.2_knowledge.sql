-- V0.6.2 运营知识库表
-- 在 ai_fleetops 库执行一次。
-- 使用 MySQL 内置 ngram 全文解析器（默认 ngram_token_size=2，无需额外配置）。

CREATE TABLE IF NOT EXISTS knowledge_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '正文',
    category VARCHAR(64) COMMENT 'SOP / 维修手册 / 历史案例 / 安全规范',
    source VARCHAR(255) COMMENT '来源',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '运营知识库';
