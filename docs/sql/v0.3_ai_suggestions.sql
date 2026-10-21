-- V0.3 AI 操作建议表
-- 在 ai_fleetops 数据库中手动执行一次即可。

CREATE TABLE IF NOT EXISTS ai_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '建议 ID',
    incident_id BIGINT NOT NULL COMMENT '关联异常事件',
    suggestion_type VARCHAR(32) NOT NULL COMMENT 'CREATE_WORK_ORDER / UPDATE_INCIDENT_STATUS / UPDATE_VEHICLE_STATUS',
    payload JSON NOT NULL COMMENT '预填充数据',
    reason TEXT COMMENT 'AI 给出建议的理由',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / EXECUTED / REJECTED',
    confirmed_by VARCHAR(50) COMMENT '确认/拒绝操作人，可空',
    executed_result JSON COMMENT '执行结果，如 {workOrderId: 123}',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_incident_id (incident_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'AI 操作建议记录';
