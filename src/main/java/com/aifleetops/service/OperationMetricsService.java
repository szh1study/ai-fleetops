package com.aifleetops.service;

import com.aifleetops.dto.AiSuggestionAdoptionMetrics;
import com.aifleetops.dto.IncidentEfficiencyMetrics;

import java.time.LocalDate;

/**
 * 运营复盘统计类指标（V0.5.1）。
 * <p>
 * 全部在现有表（incidents / work_orders / ai_suggestions）上实时聚合，不新增持久化表。
 */
public interface OperationMetricsService {

    /**
     * 异常处理时效：某天被解决的 Incident 平均耗时（按类型/严重度分组）+ 工单完成耗时。
     */
    IncidentEfficiencyMetrics getIncidentEfficiency(LocalDate date);

    /**
     * AI 建议采纳率；date 为空时统计全部，否则只统计该日创建的建议。
     */
    AiSuggestionAdoptionMetrics getAiAdoptionRate(LocalDate date);
}
