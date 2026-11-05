package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * LLM 返回的需求预测结果（V0.4.1）。
 * <p>
 * 只包含各区域预测；调度建议由后端确定性规则计算，不由 LLM 生成。
 */
@Data
public class DemandForecastResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<AreaForecast> forecasts;
}
