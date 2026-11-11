package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预测复盘项（V0.5.1）。
 * <p>
 * 对比 daily_forecasts.predicted_orders 与 orders 在相同时段内的实际下单量。
 * deviation = (实际 - 预测) / 预测 * 100%，正数表示实际高于预测（预测偏低）。
 */
@Data
public class ForecastReviewItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long forecastId;

    private LocalDate forecastDate;

    private String area;

    private String timeRange;

    private Integer predictedOrders;

    private Integer actualOrders;

    /** 偏差率（%），预测为空或 0 时为 null */
    private BigDecimal deviation;
}
