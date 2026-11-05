package com.aifleetops.ai.forecast;

import com.aifleetops.ai.dto.AreaForecast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调度规则纯函数（V0.4.2）。
 * <p>
 * AI 只预测订单量，调度数量由本规则计算：按每车承载 capacity 算出各区域需求车辆数，
 * 缺口区域按缺口降序，从盈余最大的区域调入。确定性、可单测。
 */
public final class DispatchRuleEngine {

    private DispatchRuleEngine() {
    }

    /**
     * @param forecasts    AI 预测的各区域订单量
     * @param idleVehicles 各区域当前可调度(IDLE)车辆数
     * @param capacity     每辆可调度车在每个时段预计承载的订单数
     */
    public static List<DispatchRuleSuggestion> compute(
            List<AreaForecast> forecasts,
            Map<String, Integer> idleVehicles,
            int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity 必须大于 0");
        }
        if (forecasts == null || forecasts.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> required = new HashMap<>();
        Map<String, Integer> predictedOrders = new HashMap<>();
        Map<String, Integer> deficit = new HashMap<>();
        Map<String, Integer> surplus = new HashMap<>();

        for (AreaForecast forecast : forecasts) {
            if (forecast == null || forecast.getArea() == null || forecast.getPredictedOrders() == null) {
                continue;
            }
            String area = forecast.getArea();
            int orders = Math.max(0, forecast.getPredictedOrders());
            int areaRequired = (int) Math.ceil(orders / (double) capacity);
            int areaIdle = idleVehicles == null ? 0 : idleVehicles.getOrDefault(area, 0);

            required.put(area, areaRequired);
            predictedOrders.put(area, orders);

            if (areaRequired > areaIdle) {
                deficit.put(area, areaRequired - areaIdle);
            } else if (areaIdle > areaRequired) {
                surplus.put(area, areaIdle - areaRequired);
            }
        }

        List<DispatchRuleSuggestion> suggestions = new ArrayList<>();

        List<String> deficitAreas = new ArrayList<>(deficit.keySet());
        deficitAreas.sort((a, b) -> Integer.compare(deficit.get(b), deficit.get(a)));

        for (String toArea : deficitAreas) {
            int remaining = deficit.get(toArea);
            while (remaining > 0) {
                String fromArea = findMaxSurplusArea(surplus, toArea);
                if (fromArea == null) {
                    break;
                }
                int fromSurplus = surplus.get(fromArea);
                int n = Math.min(remaining, fromSurplus);

                suggestions.add(buildSuggestion(
                        fromArea, toArea, n, predictedOrders, required, idleVehicles, fromSurplus, capacity));

                remaining -= n;
                int left = fromSurplus - n;
                if (left == 0) {
                    surplus.remove(fromArea);
                } else {
                    surplus.put(fromArea, left);
                }
            }
        }

        return suggestions;
    }

    private static DispatchRuleSuggestion buildSuggestion(
            String fromArea, String toArea, int n,
            Map<String, Integer> predictedOrders,
            Map<String, Integer> required,
            Map<String, Integer> idleVehicles,
            int fromSurplus,
            int capacity) {
        int orders = predictedOrders.getOrDefault(toArea, 0);
        int requiredCount = required.getOrDefault(toArea, 0);
        int idle = idleVehicles == null ? 0 : idleVehicles.getOrDefault(toArea, 0);
        int deficit = Math.max(0, requiredCount - idle);

        String reason = String.format(
                "预测 %s 订单 %d 单，按每车 %d 单需 %d 辆，当前可调度 %d 辆，缺口 %d 辆；从 %s（盈余 %d 辆）调入 %d 辆。",
                toArea, orders, capacity, requiredCount, idle, deficit, fromArea, fromSurplus, n);

        DispatchRuleSuggestion suggestion = new DispatchRuleSuggestion();
        suggestion.setFromArea(fromArea);
        suggestion.setToArea(toArea);
        suggestion.setVehicleCount(n);
        suggestion.setReason(reason);
        return suggestion;
    }

    private static String findMaxSurplusArea(Map<String, Integer> surplus, String excludeArea) {
        String best = null;
        int bestValue = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : surplus.entrySet()) {
            if (entry.getKey().equals(excludeArea)) {
                continue;
            }
            int value = entry.getValue();
            if (value > bestValue
                    || (value == bestValue && best != null && entry.getKey().compareTo(best) < 0)) {
                best = entry.getKey();
                bestValue = value;
            }
        }
        return best;
    }
}
