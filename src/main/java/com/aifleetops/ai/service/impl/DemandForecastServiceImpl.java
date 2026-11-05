package com.aifleetops.ai.service.impl;

import com.aifleetops.ai.dto.AreaForecast;
import com.aifleetops.ai.dto.DemandForecastGenerateRequest;
import com.aifleetops.ai.dto.DemandForecastRequest;
import com.aifleetops.ai.dto.DemandForecastResponse;
import com.aifleetops.ai.dto.DemandForecastResult;
import com.aifleetops.ai.forecast.DispatchRuleEngine;
import com.aifleetops.ai.forecast.DispatchRuleSuggestion;
import com.aifleetops.ai.provider.AiModelProvider;
import com.aifleetops.ai.service.DemandForecastService;
import com.aifleetops.ai.service.DispatchSuggestionService;
import com.aifleetops.entity.DailyForecast;
import com.aifleetops.entity.DispatchSuggestion;
import com.aifleetops.entity.Order;
import com.aifleetops.entity.Vehicle;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.service.DailyForecastService;
import com.aifleetops.service.OrderService;
import com.aifleetops.service.VehicleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需求预测编排实现（V0.4.1）。
 * <p>
 * 聚合口径：近 N 天（配置项 dispatch.historical-days）同小时段的订单量均值；可调度车辆 = vehicleStatus=IDLE。
 * AI 只负责预测订单量并给出解释，调度由 V0.4.2 的规则计算。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandForecastServiceImpl implements DemandForecastService {

    private final OrderService orderService;
    private final VehicleService vehicleService;
    private final DailyForecastService dailyForecastService;
    private final AiModelProvider aiModelProvider;
    private final DispatchSuggestionService dispatchSuggestionService;

    /** 历史订单聚合窗口天数（默认 30，开发演示可在 application.yml 配小） */
    @Value("${dispatch.historical-days:30}")
    private int historicalDays;

    /** 每辆可调度车在每个时段预计承载的订单数 */
    @Value("${dispatch.vehicle-capacity:15}")
    private int vehicleCapacity;

    @Override
    public DemandForecastResponse generate(DemandForecastGenerateRequest request) {
        validate(request);

        Map<String, Integer> historicalAvg = aggregateHistoricalAvg(request.getAreas(), request.getTimeRange());
        Map<String, Integer> currentIdle = countIdleVehicles(request.getAreas());

        DemandForecastRequest aiRequest = new DemandForecastRequest();
        aiRequest.setTargetDate(request.getTargetDate());
        aiRequest.setTimeRange(request.getTimeRange());
        aiRequest.setAreas(request.getAreas());
        aiRequest.setExternalFactors(request.getExternalFactors());
        aiRequest.setHistoricalAvg(historicalAvg);
        aiRequest.setCurrentIdleVehicles(currentIdle);

        if (!aiModelProvider.isAvailable()) {
            throw new BusinessException(503, "AI 预测当前不可用；FleetOps 主业务不受影响");
        }

        DemandForecastResult result;
        try {
            result = aiModelProvider.predictDemand(aiRequest);
        } catch (RuntimeException e) {
            log.error("AI demand forecast failed. targetDate={}, timeRange={}",
                    request.getTargetDate(), request.getTimeRange(), e);
            throw new BusinessException(500, "AI 预测调用失败，请稍后重试");
        }

        DemandForecastResponse response = new DemandForecastResponse();
        response.setTargetDate(request.getTargetDate());
        response.setTimeRange(request.getTimeRange());
        response.setCurrentIdleVehicles(currentIdle);

        Map<String, DailyForecast> areaToForecast = new HashMap<>();
        if (result != null && result.getForecasts() != null) {
            String weatherSummary = request.getExternalFactors() == null || request.getExternalFactors().isEmpty()
                    ? null
                    : String.join(",", request.getExternalFactors());
            for (AreaForecast forecast : result.getForecasts()) {
                if (forecast.getArea() == null || !request.getAreas().contains(forecast.getArea())) {
                    continue;
                }
                DailyForecast daily = new DailyForecast();
                daily.setForecastDate(request.getTargetDate());
                daily.setTimeRange(request.getTimeRange());
                daily.setArea(forecast.getArea());
                daily.setPredictedOrders(forecast.getPredictedOrders());
                daily.setPredictionReason(forecast.getReason());
                daily.setWeatherSummary(weatherSummary);
                dailyForecastService.save(daily);
                areaToForecast.put(forecast.getArea(), daily);
                response.getForecasts().add(daily);
            }
        }

        // 调度建议：规则计算 + 落库。先取代旧的 PENDING，避免重复生成堆积。
        dispatchSuggestionService.supersedePending(request.getTargetDate(), request.getTimeRange());
        List<DispatchRuleSuggestion> ruleSuggestions = DispatchRuleEngine.compute(
                result == null ? List.of() : result.getForecasts(),
                currentIdle,
                vehicleCapacity);
        for (DispatchRuleSuggestion rs : ruleSuggestions) {
            DailyForecast toForecast = areaToForecast.get(rs.getToArea());
            DispatchSuggestion suggestion = new DispatchSuggestion();
            suggestion.setForecastId(toForecast == null ? null : toForecast.getId());
            suggestion.setTargetDate(request.getTargetDate());
            suggestion.setTimeRange(request.getTimeRange());
            suggestion.setFromArea(rs.getFromArea());
            suggestion.setToArea(rs.getToArea());
            suggestion.setVehicleCount(rs.getVehicleCount());
            suggestion.setReason(rs.getReason());
            suggestion.setStatus("PENDING");
            dispatchSuggestionService.save(suggestion);
            response.getSuggestions().add(suggestion);
        }

        return response;
    }

    @Override
    public List<DailyForecast> listForecasts(LocalDate date) {
        if (date == null) {
            return dailyForecastService.list();
        }
        return dailyForecastService.list(
                Wrappers.<DailyForecast>lambdaQuery().eq(DailyForecast::getForecastDate, date));
    }

    private void validate(DemandForecastGenerateRequest request) {
        if (request == null || request.getTargetDate() == null
                || request.getTimeRange() == null || request.getTimeRange().isBlank()
                || request.getAreas() == null || request.getAreas().isEmpty()) {
            throw new BusinessException(400, "预测日期、时段和区域不能为空");
        }
    }

    private Map<String, Integer> aggregateHistoricalAvg(List<String> areas, String timeRange) {
        int[] hours = parseTimeRange(timeRange);
        LocalDateTime since = LocalDateTime.now().minusDays(historicalDays);

        List<Order> orders = orderService.list(
                Wrappers.<Order>lambdaQuery()
                        .in(Order::getArea, areas)
                        .ge(Order::getCreatedTime, since));

        Map<String, Integer> totals = new HashMap<>();
        for (Order order : orders) {
            if (order.getCreatedTime() == null || order.getArea() == null) {
                continue;
            }
            int hour = order.getCreatedTime().getHour();
            if (hour < hours[0] || hour >= hours[1]) {
                continue;
            }
            totals.merge(order.getArea(), 1, Integer::sum);
        }

        Map<String, Integer> avg = new HashMap<>();
        for (String area : areas) {
            avg.put(area, (int) Math.round(totals.getOrDefault(area, 0) / (double) historicalDays));
        }
        return avg;
    }

    private Map<String, Integer> countIdleVehicles(List<String> areas) {
        List<Vehicle> idleVehicles = vehicleService.list(
                Wrappers.<Vehicle>lambdaQuery()
                        .eq(Vehicle::getVehicleStatus, "IDLE")
                        .in(Vehicle::getCurrentArea, areas));

        Map<String, Integer> counts = new HashMap<>();
        for (String area : areas) {
            counts.put(area, 0);
        }
        for (Vehicle vehicle : idleVehicles) {
            if (vehicle.getCurrentArea() == null) {
                continue;
            }
            counts.merge(vehicle.getCurrentArea(), 1, Integer::sum);
        }
        return counts;
    }

    private int[] parseTimeRange(String timeRange) {
        String[] parts = timeRange.split("-");
        return new int[]{parseHour(parts[0]), parseHour(parts[1])};
    }

    private int parseHour(String time) {
        return Integer.parseInt(time.trim().substring(0, 2));
    }
}
