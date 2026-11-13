package com.aifleetops.service.impl;

import com.aifleetops.ai.dto.DailyReportRequest;
import com.aifleetops.ai.provider.AiModelProvider;
import com.aifleetops.dto.AiSuggestionAdoptionMetrics;
import com.aifleetops.dto.DailyReportResponse;
import com.aifleetops.dto.DispatchReviewItem;
import com.aifleetops.dto.ForecastReviewItem;
import com.aifleetops.dto.IncidentEfficiencyMetrics;
import com.aifleetops.service.OperationMetricsService;
import com.aifleetops.service.OperationReportService;
import com.aifleetops.service.OperationReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 运营日报编排实现（V0.5.2）。
 * <p>
 * 聚合当日复盘指标 -> 组装 DailyReportRequest -> 调 AI 生成摘要；
 * AI 未启用或调用失败时回退模板拼接，保证日报始终有内容可展示。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationReportServiceImpl implements OperationReportService {

    private final OperationReviewService operationReviewService;
    private final OperationMetricsService operationMetricsService;
    private final AiModelProvider aiModelProvider;

    @Override
    public DailyReportResponse generateDailyReport(LocalDate date) {
        List<ForecastReviewItem> forecastReviews = operationReviewService.listForecastReview(date);
        List<DispatchReviewItem> dispatchReviews = operationReviewService.listDispatchReview(date);
        IncidentEfficiencyMetrics incidentMetrics = operationMetricsService.getIncidentEfficiency(date);
        // 采纳率口径为累计（与 V0.5.1 页面一致），不按天过滤
        AiSuggestionAdoptionMetrics aiAdoption = operationMetricsService.getAiAdoptionRate(null);

        DailyReportRequest request = new DailyReportRequest();
        request.setReportDate(date);
        request.setForecastReviews(forecastReviews);
        request.setDispatchReviews(dispatchReviews);
        request.setIncidentMetrics(incidentMetrics);
        request.setAiAdoption(aiAdoption);
        request.setOverallDeviation(overallDeviation(forecastReviews));

        DailyReportResponse response = new DailyReportResponse();
        response.setReportDate(date);

        String summary;
        boolean aiGenerated = false;
        if (aiModelProvider.isAvailable()) {
            try {
                summary = aiModelProvider.generateDailyReport(request);
                aiGenerated = true;
            } catch (RuntimeException e) {
                log.error("AI daily report generation failed, fallback to template. date={}, provider={}",
                        date, aiModelProvider.getProviderName(), e);
                summary = buildFallbackSummary(request);
            }
        } else {
            summary = buildFallbackSummary(request);
        }
        response.setSummary(summary);
        response.setAiGenerated(aiGenerated);
        return response;
    }

    /** 整体偏差 = 各预测行偏差绝对值的平均；无有效偏差时返回 null。 */
    private BigDecimal overallDeviation(List<ForecastReviewItem> items) {
        List<BigDecimal> devs = items.stream()
                .map(ForecastReviewItem::getDeviation)
                .filter(Objects::nonNull)
                .map(BigDecimal::abs)
                .toList();
        if (devs.isEmpty()) {
            return null;
        }
        BigDecimal sum = devs.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(devs.size()), 2, RoundingMode.HALF_UP);
    }

    private String buildFallbackSummary(DailyReportRequest r) {
        String deviation = r.getOverallDeviation() == null ? "暂无数据" : r.getOverallDeviation() + "%";
        String avgMinutes = r.getIncidentMetrics() == null || r.getIncidentMetrics().getAvgResolutionMinutes() == null
                ? "暂无数据" : r.getIncidentMetrics().getAvgResolutionMinutes() + " 分钟";
        String adoption = r.getAiAdoption() == null || r.getAiAdoption().getAdoptionRate() == null
                ? "暂无数据" : r.getAiAdoption().getAdoptionRate() + "%";
        int dispatchCount = r.getDispatchReviews() == null ? 0 : r.getDispatchReviews().size();
        return String.format("%s 运营日报：预测整体偏差 %s，调度建议 %d 条，异常平均处理时长 %s，AI 建议采纳率 %s。",
                r.getReportDate(), deviation, dispatchCount, avgMinutes, adoption);
    }
}
