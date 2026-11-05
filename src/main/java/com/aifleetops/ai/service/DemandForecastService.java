package com.aifleetops.ai.service;

import com.aifleetops.ai.dto.DemandForecastGenerateRequest;
import com.aifleetops.ai.dto.DemandForecastResponse;
import com.aifleetops.entity.DailyForecast;

import java.time.LocalDate;
import java.util.List;

/**
 * 需求预测编排服务（V0.4.1）。
 * <p>
 * 负责聚合历史订单、统计可调度车辆、调用 AI Provider、落库 daily_forecasts。
 */
public interface DemandForecastService {

    /**
     * 生成未来某时段各区域的订单量预测，并落库。
     */
    DemandForecastResponse generate(DemandForecastGenerateRequest request);

    /**
     * 查询历史预测；date 为空时返回全部。
     */
    List<DailyForecast> listForecasts(LocalDate date);
}
