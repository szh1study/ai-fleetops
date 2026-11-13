package com.aifleetops.ai.dto;

import com.aifleetops.dto.AiSuggestionAdoptionMetrics;
import com.aifleetops.dto.DispatchReviewItem;
import com.aifleetops.dto.ForecastReviewItem;
import com.aifleetops.dto.IncidentEfficiencyMetrics;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 运营日报生成请求（V0.5.2）。
 * <p>
 * 由 OperationReportService 聚合当日复盘指标后组装，交给 AiModelProvider 生成文字摘要。
 */
@Data
public class DailyReportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate reportDate;

    /** 整体偏差 = 各预测行偏差绝对值的平均（%），可空 */
    private BigDecimal overallDeviation;

    private List<ForecastReviewItem> forecastReviews;

    private List<DispatchReviewItem> dispatchReviews;

    private IncidentEfficiencyMetrics incidentMetrics;

    private AiSuggestionAdoptionMetrics aiAdoption;
}
