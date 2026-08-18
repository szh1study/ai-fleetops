package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.entity.DailyForecast;
import com.aifleetops.service.DailyForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/daily-forecasts")
@RequiredArgsConstructor
public class DailyForecastController {

    private final DailyForecastService dailyForecastService;

    @GetMapping
    public Result<List<DailyForecast>> list() {
        return Result.success(dailyForecastService.list());
    }

    @GetMapping("/{id}")
    public Result<DailyForecast> getById(@PathVariable Long id) {
        return Result.success(dailyForecastService.getById(id));
    }

    @PostMapping
    public Result<DailyForecast> create(@RequestBody DailyForecast dailyForecast) {
        dailyForecastService.save(dailyForecast);
        return Result.success(dailyForecast);
    }

    @PutMapping("/{id}")
    public Result<DailyForecast> update(@PathVariable Long id, @RequestBody DailyForecast dailyForecast) {
        dailyForecast.setId(id);
        dailyForecastService.updateById(dailyForecast);
        return Result.success(dailyForecast);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dailyForecastService.removeById(id);
        return Result.success();
    }
}
