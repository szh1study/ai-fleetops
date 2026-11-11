package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 调度效果复盘项（V0.5.1）。
 * <p>
 * 基于 dispatch_suggestions 的执行结果 + 目标区域（to_area）在目标时段内的实际订单量，
 * 评估调度建议是否派上用场。predicted_orders 通过 forecastId 回填 daily_forecasts。
 */
@Data
public class DispatchReviewItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long suggestionId;

    private LocalDate targetDate;

    private String timeRange;

    private String fromArea;

    private String toArea;

    private Integer vehicleCount;

    /** PENDING / EXECUTED / REJECTED */
    private String status;

    /** 目标区域预测订单（来自关联 daily_forecasts，可能为 null） */
    private Integer predictedOrders;

    /** 目标区域在时段内的实际订单 */
    private Integer actualOrders;

    /** 偏差率（%），预测为空或 0 时为 null */
    private BigDecimal deviation;

    /** 需求是否满足：预测非空且 actual >= predicted */
    private Boolean demandMet;
}
