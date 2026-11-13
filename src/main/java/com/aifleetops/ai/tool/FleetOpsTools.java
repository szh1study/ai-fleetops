package com.aifleetops.ai.tool;

import com.aifleetops.entity.DailyForecast;
import com.aifleetops.entity.Incident;
import com.aifleetops.entity.Vehicle;
import com.aifleetops.service.DailyForecastService;
import com.aifleetops.service.IncidentService;
import com.aifleetops.service.VehicleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI 助手只读工具集（V0.6.1）。
 * <p>
 * 方法用 Spring AI 的 {@code @Tool} 标注，注册到 ChatClient 后由模型在需要时调用。
 * 所有工具只读，不修改任何业务数据。
 */
@Component
@RequiredArgsConstructor
public class FleetOpsTools {

    private final VehicleService vehicleService;
    private final IncidentService incidentService;
    private final DailyForecastService dailyForecastService;

    @Tool(name = "queryVehicles", description = """
            查询车辆的当前状态与分布。当用户问“今天车怎么样”“某区域有多少车”“空闲车有多少”时调用。
            返回匹配车辆的数量、各状态数量分布，以及最多 12 个车辆编号（vehicleCode）。
            area 传 null 表示不限区域（查询全部），status 传 null 表示不限状态。
            """)
    public String queryVehicles(
            @ToolParam(required = false, description = "区域名称，只能是 海淀区、朝阳区、西城区 之一；传 null 表示全部区域") String area,
            @ToolParam(required = false, description = "车辆状态，只能是 IDLE(空闲)、READY(可运营)、OPERATING(运营中)、CHARGING(充电中)、MAINTENANCE(维修中)、OFFLINE(离线) 之一；传 null 表示不限状态") String status) {
        LambdaQueryWrapper<Vehicle> q = Wrappers.<Vehicle>lambdaQuery();
        if (hasText(area)) {
            q.eq(Vehicle::getCurrentArea, area);
        }
        if (hasText(status)) {
            q.eq(Vehicle::getVehicleStatus, status);
        }
        List<Vehicle> list = vehicleService.list(q);
        if (list.isEmpty()) {
            return "未查询到符合条件的车辆。";
        }
        Map<String, Long> byStatus = list.stream().collect(Collectors.groupingBy(
                v -> v.getVehicleStatus() == null ? "UNKNOWN" : v.getVehicleStatus(),
                Collectors.counting()));
        String statusLine = byStatus.entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue() + " 辆")
                .collect(Collectors.joining("、"));
        String codes = list.stream()
                .map(Vehicle::getVehicleCode)
                .filter(Objects::nonNull)
                .limit(12)
                .collect(Collectors.joining("、"));
        return String.format("共 %d 辆车：%s。车辆编号：%s。", list.size(), statusLine, codes);
    }

    @Tool(name = "queryIncidents", description = """
            查询异常事件的数量与状态分布。当用户问“有哪些异常”“有没有未处理的告警”“还有几个没解决”时调用。
            返回匹配异常的总数，以及按状态（OPEN、ANALYZING、PROCESSING、RESOLVED 等）分布的数量。
            status 传 null 表示不限状态，severity 传 null 表示不限严重度。
            """)
    public String queryIncidents(
            @ToolParam(required = false, description = "异常状态，只能是 OPEN(待处理)、ANALYZING(分析中)、PENDING_APPROVAL(待审批)、PROCESSING(处理中)、RESOLVED(已解决)、REJECTED(已拒绝) 之一；传 null 表示不限状态") String status,
            @ToolParam(required = false, description = "严重度，只能是 LOW(低)、MEDIUM(中)、HIGH(高)、CRITICAL(严重) 之一；传 null 表示不限严重度") String severity) {
        LambdaQueryWrapper<Incident> q = Wrappers.<Incident>lambdaQuery();
        if (hasText(status)) {
            q.eq(Incident::getStatus, status);
        }
        if (hasText(severity)) {
            q.eq(Incident::getSeverity, severity);
        }
        List<Incident> list = incidentService.list(q);
        if (list.isEmpty()) {
            return "未查询到符合条件的异常。";
        }
        Map<String, Long> byStatus = list.stream().collect(Collectors.groupingBy(
                i -> i.getStatus() == null ? "UNKNOWN" : i.getStatus(),
                Collectors.counting()));
        String statusLine = byStatus.entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue() + " 条")
                .collect(Collectors.joining("、"));
        return String.format("共 %d 条异常：%s。", list.size(), statusLine);
    }

    @Tool(name = "queryForecast", description = """
            查询某日期某时段已有的订单预测（只读，不生成新预测）。当用户问“某天某时段预测多少单”“哪个区域预测最高”时调用。
            返回各区域在该时段的预测订单量（predictedOrders），供判断哪个区域需求高。
            只能返回已经生成过的预测；查不到时明确告知用户先去“需求预测”页面生成，绝不编造预测数字。
            """)
    public String queryForecast(
            @ToolParam(description = "预测日期，格式 yyyy-MM-dd，如 2026-11-13") String date,
            @ToolParam(required = false, description = "时段，格式 HH:mm-HH:mm，如 20:00-24:00；传 null 表示当天全部时段") String timeRange) {
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (RuntimeException e) {
            return "日期格式应为 yyyy-MM-dd，如 2026-11-13。";
        }
        List<DailyForecast> list = dailyForecastService.list(Wrappers.<DailyForecast>lambdaQuery()
                .eq(DailyForecast::getForecastDate, d)
                .eq(hasText(timeRange), DailyForecast::getTimeRange, timeRange));
        if (list.isEmpty()) {
            return "暂无 " + date + (hasText(timeRange) ? " " + timeRange : "") + " 的预测数据，请先在需求预测页生成。";
        }
        return list.stream()
                .map(f -> (f.getArea() == null ? "未知区域" : f.getArea())
                        + (f.getTimeRange() == null ? "" : " " + f.getTimeRange())
                        + " 预测 " + (f.getPredictedOrders() == null ? 0 : f.getPredictedOrders()) + " 单")
                .collect(Collectors.joining("；"));
    }

    @Tool(name = "queryWeather", description = """
            查询某城市某日天气。当用户问“今天天气怎么样”“北京明天会不会下雨”时调用。
            返回天气现象（晴、多云、小雨）与温度（℃），可用于判断天气对用车需求的影响。
            V0.6.1 为模拟数据，未接真实天气 API。
            """)
    public String queryWeather(
            @ToolParam(description = "城市名称，如 北京、上海") String city,
            @ToolParam(description = "日期，格式 yyyy-MM-dd，如 2026-11-13") String date) {
        int h = (city + "|" + date).hashCode();
        String[] weathers = {"晴", "多云", "小雨"};
        String weather = weathers[Math.floorMod(h, weathers.length)];
        int temp = 15 + Math.floorMod(h, 10);
        String hint = "小雨".equals(weather) ? "；雨天晚高峰用车需求通常上升" : "";
        return String.format("%s %s：%s，%d℃（V0.6.1 模拟天气数据，未接真实 API）%s",
                city, date, weather, temp, hint);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
