package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 运营日报响应（V0.5.2）。
 */
@Data
public class DailyReportResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate reportDate;

    private String summary;

    /** true = AI 生成；false = 模板兜底（AI 未启用或调用失败） */
    private boolean aiGenerated;
}
