package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 异常处理时效（V0.5.1）。
 * <p>
 * 统计某天被解决的 Incident 从创建到解决的平均耗时，并附工单从创建到完成的平均耗时。
 */
@Data
public class IncidentEfficiencyMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate date;

    private Integer resolvedCount;

    private Long avgResolutionMinutes;

    private Long minResolutionMinutes;

    private Long maxResolutionMinutes;

    /** 按异常类型分组 */
    private List<GroupStat> byType;

    /** 按严重度分组 */
    private List<GroupStat> bySeverity;

    private Integer completedWorkOrderCount;

    private Long avgWorkOrderCompletionMinutes;

    @Data
    public static class GroupStat implements Serializable {

        private static final long serialVersionUID = 1L;

        private String key;

        private Integer count;

        private Long avgMinutes;
    }
}
