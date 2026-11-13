package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.dto.DailyReportResponse;
import com.aifleetops.service.OperationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 运营日报接口（V0.5.2）。
 */
@RestController
@RequestMapping("/api/operation-reports")
@RequiredArgsConstructor
public class OperationReportController {

    private final OperationReportService operationReportService;

    @GetMapping("/daily")
    public Result<DailyReportResponse> daily(@RequestParam(required = false) LocalDate date) {
        return Result.success(operationReportService.generateDailyReport(
                date == null ? LocalDate.now() : date));
    }
}
