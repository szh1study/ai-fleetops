package com.aifleetops.ai.forecast;

import com.aifleetops.ai.dto.AreaForecast;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 调度规则引擎单测（纯函数，无 Spring 上下文）。
 */
class DispatchRuleEngineTest {

    private AreaForecast forecast(String area, int orders) {
        AreaForecast f = new AreaForecast();
        f.setArea(area);
        f.setPredictedOrders(orders);
        return f;
    }

    @Test
    void balanced_returnsNoSuggestion() {
        List<AreaForecast> forecasts = List.of(forecast("海淀区", 30));
        Map<String, Integer> idle = Map.of("海淀区", 2); // 30/15=2 需 2 辆，正好
        assertTrue(DispatchRuleEngine.compute(forecasts, idle, 15).isEmpty());
    }

    @Test
    void singleDeficit_singleSurplus_returnsOneSuggestion() {
        List<AreaForecast> forecasts = List.of(
                forecast("朝阳区", 45),  // 需 3 辆
                forecast("海淀区", 15)); // 需 1 辆
        Map<String, Integer> idle = Map.of("朝阳区", 1, "海淀区", 3); // 朝阳缺 2，海淀盈 2

        List<DispatchRuleSuggestion> result = DispatchRuleEngine.compute(forecasts, idle, 15);

        assertEquals(1, result.size());
        assertEquals("海淀区", result.get(0).getFromArea());
        assertEquals("朝阳区", result.get(0).getToArea());
        assertEquals(2, result.get(0).getVehicleCount());
    }

    @Test
    void deficitGreaterThanSurplus_limitedToSurplus() {
        List<AreaForecast> forecasts = List.of(
                forecast("朝阳区", 150), // 需 10 辆
                forecast("海淀区", 0));  // 需 0 辆，盈余 = idle
        Map<String, Integer> idle = Map.of("朝阳区", 0, "海淀区", 3);

        List<DispatchRuleSuggestion> result = DispatchRuleEngine.compute(forecasts, idle, 15);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getVehicleCount());
    }

    @Test
    void zeroPrediction_returnsNoSuggestion() {
        List<AreaForecast> forecasts = List.of(forecast("海淀区", 0));
        Map<String, Integer> idle = Map.of("海淀区", 0);
        assertTrue(DispatchRuleEngine.compute(forecasts, idle, 15).isEmpty());
    }

    @Test
    void reason_isPopulatedWithKeyNumbers() {
        List<AreaForecast> forecasts = List.of(
                forecast("朝阳区", 45),
                forecast("海淀区", 15));
        Map<String, Integer> idle = Map.of("朝阳区", 1, "海淀区", 3);

        List<DispatchRuleSuggestion> result = DispatchRuleEngine.compute(forecasts, idle, 15);

        assertEquals(1, result.size());
        String reason = result.get(0).getReason();
        assertNotNull(reason);
        assertTrue(reason.contains("朝阳区"));
        assertTrue(reason.contains("海淀区"));
        assertTrue(reason.contains("2 辆"));
    }
}
