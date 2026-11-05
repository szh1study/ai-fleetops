package com.aifleetops.ai.forecast;

import lombok.Data;

/**
 * 调度规则引擎的输出（纯计算结果，不落库）。
 * <p>
 * 与实体 {@link com.aifleetops.entity.DispatchSuggestion} 分离：
 * 规则引擎只产出 fromArea/toArea/vehicleCount/reason，持久化字段由编排层填充。
 */
@Data
public class DispatchRuleSuggestion {

    private String fromArea;

    private String toArea;

    private Integer vehicleCount;

    private String reason;
}
