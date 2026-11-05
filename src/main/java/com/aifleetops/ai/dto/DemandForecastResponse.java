package com.aifleetops.ai.dto;

import com.aifleetops.entity.DailyForecast;
import com.aifleetops.entity.DispatchSuggestion;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需求预测生成接口的响应（V0.4）。
 * <p>
 * 返回已落库的预测行 + 各区域当前可调度车辆数 + 规则计算出的调度建议。
 */
@Data
public class DemandForecastResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate targetDate;

    private String timeRange;

    /** area -> 当前可调度(IDLE)车辆数 */
    private Map<String, Integer> currentIdleVehicles = new HashMap<>();

    /** 已落库的预测行（含 id） */
    private List<DailyForecast> forecasts = new ArrayList<>();

    /** 已落库的调度建议（含 id） */
    private List<DispatchSuggestion> suggestions = new ArrayList<>();
}
