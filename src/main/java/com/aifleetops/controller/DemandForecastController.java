package com.aifleetops.controller;

import com.aifleetops.ai.dto.DemandForecastGenerateRequest;
import com.aifleetops.ai.dto.DemandForecastResponse;
import com.aifleetops.ai.service.DemandForecastService;
import com.aifleetops.common.Result;
import com.aifleetops.entity.DailyForecast;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * V0.4.1 需求预测接口。
 * <p>
 * 生成预测是编排入口（聚合 + AI + 落库）；查询复用 daily_forecasts。
 * 调度建议的确认/拒绝在 V0.4.2 再加。
 */
@RestController
@RequestMapping("/api/demand-forecasts")
@RequiredArgsConstructor
public class DemandForecastController {

    private final DemandForecastService demandForecastService;

    @PostMapping("/generate")
    public Result<DemandForecastResponse> generate(@RequestBody DemandForecastGenerateRequest request) {
        return Result.success(demandForecastService.generate(request));
    }

    @GetMapping
    public Result<List<DailyForecast>> list(@RequestParam(required = false) LocalDate date) {
        return Result.success(demandForecastService.listForecasts(date));
    }
}
