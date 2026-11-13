package com.aifleetops.service;

import com.aifleetops.dto.DailyReportResponse;

import java.time.LocalDate;

/**
 * 运营日报服务（V0.5.2）。
 */
public interface OperationReportService {

    /**
     * 生成某天的运营日报文字摘要：AI 可用时走模型，否则（或调用失败时）回退模板拼接。
     */
    DailyReportResponse generateDailyReport(LocalDate date);
}
