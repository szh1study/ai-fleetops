-- V0.4.2 车辆调度建议表
-- 在 ai_fleetops 库执行一次。

CREATE TABLE IF NOT EXISTS dispatch_suggestions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '建议 ID',
    forecast_id BIGINT COMMENT '关联 daily_forecasts 行（to_area 的预测行）',
    target_date DATE NOT NULL COMMENT '目标日期',
    time_range VARCHAR(32) NOT NULL COMMENT '时段，如 20:00-24:00',
    from_area VARCHAR(64) NOT NULL COMMENT '调出区域',
    to_area VARCHAR(64) NOT NULL COMMENT '调入区域',
    vehicle_count INT NOT NULL COMMENT '建议调动车辆数',
    reason TEXT COMMENT '规则生成的调度理由',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / EXECUTED / REJECTED',
    confirmed_by VARCHAR(50) COMMENT '操作人，可空',
    executed_result JSON COMMENT '执行结果，如 {movedVehicleIds: [1, 3]}',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_forecast_id (forecast_id),
    INDEX idx_status (status),
    INDEX idx_target_date (target_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '车辆调度建议记录';
