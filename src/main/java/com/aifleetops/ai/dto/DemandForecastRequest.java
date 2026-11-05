package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 后端组装好、发给 LLM 的需求预测上下文（V0.4.1）。
 * <p>
 * {@code historicalAvg} 与 {@code currentIdleVehicles} 由后端从 orders / vehicles 聚合得到，
 * LLM 不查库，只基于给定数字预测订单量。
 */
@Data
public class DemandForecastRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate targetDate;

    private String timeRange;

    private List<String> areas;

    private List<String> externalFactors;

    /** area -> 历史同期平均订单 */
    private Map<String, Integer> historicalAvg;

    /** area -> 当前可调度(IDLE)车辆数 */
    private Map<String, Integer> currentIdleVehicles;
}
