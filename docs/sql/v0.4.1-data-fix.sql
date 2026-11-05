-- V0.4.1 数据修复：统一区域词汇为中文，并补充可调度(IDLE)车辆
-- 在 ai_fleetops 库执行一次即可。

-- 1. 统一 orders.area 为中文（与 vehicles.current_area 对齐）
--    历史数据里近 30 天的订单用了英文 CBD / Airport，与车辆的中文区域不一致。
UPDATE orders SET area = '海淀区' WHERE area = 'CBD';
UPDATE orders SET area = '朝阳区' WHERE area = 'Airport';

-- 2. 补两辆 IDLE 空闲车（V0.4 可调度口径 = vehicle_status = IDLE）
--    原数据只有 READY / OPERATING / MAINTENANCE，导致"当前可调度"恒为 0。
UPDATE vehicles SET vehicle_status = 'IDLE' WHERE id = 1;
UPDATE vehicles SET vehicle_status = 'IDLE', current_area = '朝阳区' WHERE id = 3;
