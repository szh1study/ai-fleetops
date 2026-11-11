package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.dto.AiSuggestionAdoptionMetrics;
import com.aifleetops.dto.IncidentEfficiencyMetrics;
import com.aifleetops.service.OperationMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 运营复盘统计指标接口（V0.5.1）。
 */
@RestController
@RequestMapping("/api/operation-metrics")
@RequiredArgsConstructor
public class OperationMetricsController {

    private final OperationMetricsService operationMetricsService;

    @GetMapping("/incident-efficiency")
    public Result<IncidentEfficiencyMetrics> incidentEfficiency(
            @RequestParam(required = false) LocalDate date) {
        return Result.success(operationMetricsService.getIncidentEfficiency(date));
    }

    @GetMapping("/ai-adoption")
    public Result<AiSuggestionAdoptionMetrics> aiAdoption(
            @RequestParam(required = false) LocalDate date) {
        return Result.success(operationMetricsService.getAiAdoptionRate(date));
    }
}
