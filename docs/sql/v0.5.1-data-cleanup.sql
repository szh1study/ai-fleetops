-- V0.5.1 数据清洗：去重 daily_forecasts + 清空调度建议
-- 在 ai_fleetops 库执行一次。

-- 1. 删除 daily_forecasts 重复记录，每个 (forecast_date, area, time_range) 只保留最新一条
--    （后端复盘查询也会做读侧去重，本步只是清理历史冗余）
DELETE df1 FROM daily_forecasts df1
INNER JOIN daily_forecasts df2
  ON df1.forecast_date = df2.forecast_date
 AND df1.area = df2.area
 AND df1.time_range = df2.time_range
 AND df1.id < df2.id;

-- 2. 清空调度建议（重新生成预测时会自动创建新的 PENDING 建议）
TRUNCATE TABLE dispatch_suggestions;

-- 3. （可选）如需重新演示 V0.3 的 AI 建议确认流程，可清空建议后重跑；
--    如想保留历史采纳记录给 V0.5.1 采纳率饼图，则不要执行下面这句。
-- TRUNCATE TABLE ai_suggestions;
