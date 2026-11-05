package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 前端发起需求预测的入参（V0.4.1）。
 * <p>
 * 只描述"预测什么"，历史均值与当前车辆数由后端聚合统计后填入 {@link DemandForecastRequest}。
 */
@Data
public class DemandForecastGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 预测日期 */
    private LocalDate targetDate;

    /** 时段，如 "20:00-24:00" */
    private String timeRange;

    /** 预测区域（英文，如 CBD） */
    private List<String> areas;

    /** 外部因素标签，如 ["演唱会", "下雨"] */
    private List<String> externalFactors;
}
