-- V0.5.1 演示数据：让「运营复盘」页面展示有意义的数据
-- 在 ai_fleetops 库执行一次。前提：incidents / work_orders / vehicles 已有 V0.1 基础数据。

-- 1. 补充 2026-11-01 20:00-24:00 多区域订单（与需求预测的时段对齐）
--    实际订单量由此确定：海淀区 3、朝阳区 1、西城区 1；预测值取决于 AI 输出。
INSERT INTO orders (order_code, area, vehicle_id, status, created_time, completed_time, wait_minutes, created_at, updated_at) VALUES
('ORD-20261101-005', '海淀区', 1, 'COMPLETED', '2026-11-01 20:10:00', '2026-11-01 20:40:00', 3.00, NOW(), NOW()),
('ORD-20261101-006', '海淀区', 3, 'COMPLETED', '2026-11-01 20:20:00', '2026-11-01 20:50:00', 4.00, NOW(), NOW()),
('ORD-20261101-007', '海淀区', 1, 'COMPLETED', '2026-11-01 20:30:00', '2026-11-01 21:00:00', 5.00, NOW(), NOW()),
('ORD-20261101-008', '朝阳区', 2, 'COMPLETED', '2026-11-01 20:15:00', '2026-11-01 20:45:00', 6.00, NOW(), NOW()),
('ORD-20261101-009', '西城区', 4, 'COMPLETED', '2026-11-01 20:25:00', '2026-11-01 20:55:00', 4.50, NOW(), NOW());

-- 2. 两条 incident 都在 2026-11-01 创建并解决（保证「当日解决」统计有数，且耗时非 0）
UPDATE incidents
   SET created_at = '2026-11-01 15:00:00', status = 'RESOLVED', resolved_at = '2026-11-01 15:30:00'
 WHERE id = 1;
UPDATE incidents
   SET created_at = '2026-11-01 16:00:00', status = 'RESOLVED', resolved_at = '2026-11-01 17:00:00'
 WHERE id = 2;

-- 3. 对应工单在当天完成（先完成后解决，耗时非 0）
UPDATE work_orders
   SET created_at = '2026-11-01 14:50:00', status = 'COMPLETED', completed_at = '2026-11-01 15:10:00'
 WHERE incident_id = 1;
UPDATE work_orders
   SET created_at = '2026-11-01 16:10:00', status = 'COMPLETED', completed_at = '2026-11-01 16:40:00'
 WHERE incident_id = 2;

-- 4. 补 ai_suggestions 三态记录，让采纳率饼图非 0（采纳率按累计统计，与日期无关）
--    如 V0.3 测试已留有建议记录，可跳过本段。
INSERT INTO ai_suggestions (incident_id, suggestion_type, payload, reason, status, created_at, updated_at) VALUES
(1, 'CREATE_WORK_ORDER', '{}', '演示数据：已采纳', 'EXECUTED', '2026-11-01 14:00:00', NOW()),
(1, 'UPDATE_INCIDENT_STATUS', '{}', '演示数据：已采纳', 'EXECUTED', '2026-11-01 14:05:00', NOW()),
(2, 'CREATE_WORK_ORDER', '{}', '演示数据：已拒绝', 'REJECTED', '2026-11-01 14:10:00', NOW()),
(3, 'UPDATE_VEHICLE_STATUS', '{}', '演示数据：待处理', 'PENDING', '2026-11-01 14:15:00', NOW());
