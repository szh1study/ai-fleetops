package com.aifleetops.service.impl;

import com.aifleetops.ai.service.DispatchSuggestionService;
import com.aifleetops.dto.DispatchReviewItem;
import com.aifleetops.dto.ForecastReviewItem;
import com.aifleetops.entity.DailyForecast;
import com.aifleetops.entity.DispatchSuggestion;
import com.aifleetops.entity.OperationReview;
import com.aifleetops.entity.Order;
import com.aifleetops.mapper.OperationReviewMapper;
import com.aifleetops.service.DailyForecastService;
import com.aifleetops.service.OperationReviewService;
import com.aifleetops.service.OrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营复盘实现（V0.5.1）：预测复盘 + 调度效果复盘。
 * <p>
 * 全部在现有表上实时计算，不新增持久化表：
 * 预测复盘 = daily_forecasts.predicted_orders vs orders 实际下单量；
 * 调度复盘 = dispatch_suggestions 执行结果 + 目标区域实际订单量。
 */
@Service
@RequiredArgsConstructor
public class OperationReviewServiceImpl extends ServiceImpl<OperationReviewMapper, OperationReview>
        implements OperationReviewService {

    private final DailyForecastService dailyForecastService;
    private final OrderService orderService;
    private final DispatchSuggestionService dispatchSuggestionService;

    @Override
    public List<ForecastReviewItem> listForecastReview(LocalDate date) {
        List<DailyForecast> forecasts = latestForecasts(date);
        Map<String, List<Order>> ordersByArea = loadOrdersByArea(date);

        List<ForecastReviewItem> items = new ArrayList<>();
        for (DailyForecast f : forecasts) {
            int[] hours = parseHours(f.getTimeRange());
            Integer actual = hours == null
                    ? null
                    : countOrdersInWindow(ordersByArea.get(f.getArea()), date, hours[0], hours[1]);

            ForecastReviewItem item = new ForecastReviewItem();
            item.setForecastId(f.getId());
            item.setForecastDate(f.getForecastDate());
            item.setArea(f.getArea());
            item.setTimeRange(f.getTimeRange());
            item.setPredictedOrders(f.getPredictedOrders());
            item.setActualOrders(actual);
            item.setDeviation(deviation(actual, f.getPredictedOrders()));
            items.add(item);
        }
        return items;
    }

    @Override
    public List<DispatchReviewItem> listDispatchReview(LocalDate date) {
        List<DispatchSuggestion> suggestions = dispatchSuggestionService.list(
                Wrappers.<DispatchSuggestion>lambdaQuery().eq(DispatchSuggestion::getTargetDate, date));

        // forecastId -> predictedOrders，用于回填目标区域预测需求
        Map<Long, Integer> predictedByForecastId = new HashMap<>();
        for (DailyForecast f : latestForecasts(date)) {
            predictedByForecastId.put(f.getId(), f.getPredictedOrders());
        }

        Map<String, List<Order>> ordersByArea = loadOrdersByArea(date);

        List<DispatchReviewItem> items = new ArrayList<>();
        for (DispatchSuggestion s : suggestions) {
            int[] hours = parseHours(s.getTimeRange());
            Integer actual = hours == null
                    ? null
                    : countOrdersInWindow(ordersByArea.get(s.getToArea()), date, hours[0], hours[1]);
            Integer predicted = s.getForecastId() == null
                    ? null
                    : predictedByForecastId.get(s.getForecastId());

            DispatchReviewItem item = new DispatchReviewItem();
            item.setSuggestionId(s.getId());
            item.setTargetDate(s.getTargetDate());
            item.setTimeRange(s.getTimeRange());
            item.setFromArea(s.getFromArea());
            item.setToArea(s.getToArea());
            item.setVehicleCount(s.getVehicleCount());
            item.setStatus(s.getStatus());
            item.setPredictedOrders(predicted);
            item.setActualOrders(actual);
            item.setDeviation(deviation(actual, predicted));
            item.setDemandMet(predicted != null && actual != null && actual >= predicted);
            items.add(item);
        }
        return items;
    }

    // ---- 复用逻辑 ----

    /** 取某天 (area, timeRange) 的最新预测各一条，避免重复生成导致复盘表出现重复行。 */
    private List<DailyForecast> latestForecasts(LocalDate date) {
        List<DailyForecast> all = dailyForecastService.list(
                Wrappers.<DailyForecast>lambdaQuery()
                        .eq(DailyForecast::getForecastDate, date)
                        .orderByDesc(DailyForecast::getId));
        Map<String, DailyForecast> latest = new LinkedHashMap<>();
        for (DailyForecast f : all) {
            latest.putIfAbsent(f.getArea() + "|" + f.getTimeRange(), f);
        }
        return new ArrayList<>(latest.values());
    }

    private Map<String, List<Order>> loadOrdersByArea(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        Map<String, List<Order>> byArea = new HashMap<>();
        for (Order o : orderService.list(
                Wrappers.<Order>lambdaQuery()
                        .ge(Order::getCreatedTime, start)
                        .lt(Order::getCreatedTime, end))) {
            if (o.getArea() == null || o.getCreatedTime() == null) {
                continue;
            }
            byArea.computeIfAbsent(o.getArea(), k -> new ArrayList<>()).add(o);
        }
        return byArea;
    }

    private Integer countOrdersInWindow(List<Order> orders, LocalDate date, int startHour, int endHour) {
        if (orders == null || orders.isEmpty()) {
            return 0;
        }
        LocalDateTime start = date.atStartOfDay().plusHours(startHour);
        LocalDateTime end = date.atStartOfDay().plusHours(endHour);
        int count = 0;
        for (Order o : orders) {
            LocalDateTime t = o.getCreatedTime();
            if (!t.isBefore(start) && t.isBefore(end)) {
                count++;
            }
        }
        return count;
    }

    /** 偏差率 = (实际 - 预测) / 预测 * 100%，正 = 实际高于预测（预测偏低）。预测为空或 0 时返回 null。 */
    private BigDecimal deviation(Integer actual, Integer predicted) {
        if (actual == null || predicted == null || predicted == 0) {
            return null;
        }
        double d = (actual - predicted) * 100.0 / predicted;
        return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP);
    }

    /** 解析 "20:00-24:00" 为 [20, 24]；无法解析返回 null。 */
    private int[] parseHours(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) {
            return null;
        }
        try {
            String[] parts = timeRange.split("-");
            return new int[]{parseHour(parts[0]), parseHour(parts[1])};
        } catch (RuntimeException e) {
            return null;
        }
    }

    private int parseHour(String time) {
        String hh = time.trim();
        int colon = hh.indexOf(':');
        if (colon > 0) {
            hh = hh.substring(0, colon);
        }
        return Integer.parseInt(hh.trim());
    }
}
