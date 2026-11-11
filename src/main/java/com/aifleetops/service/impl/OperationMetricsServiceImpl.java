package com.aifleetops.service.impl;

import com.aifleetops.ai.service.AiSuggestionService;
import com.aifleetops.dto.AiSuggestionAdoptionMetrics;
import com.aifleetops.dto.IncidentEfficiencyMetrics;
import com.aifleetops.entity.AiSuggestion;
import com.aifleetops.entity.Incident;
import com.aifleetops.entity.WorkOrder;
import com.aifleetops.service.IncidentService;
import com.aifleetops.service.OperationMetricsService;
import com.aifleetops.service.WorkOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 运营复盘统计实现（V0.5.1）。
 */
@Service
@RequiredArgsConstructor
public class OperationMetricsServiceImpl implements OperationMetricsService {

    private final IncidentService incidentService;
    private final WorkOrderService workOrderService;
    private final AiSuggestionService aiSuggestionService;

    @Override
    public IncidentEfficiencyMetrics getIncidentEfficiency(LocalDate date) {
        List<Incident> resolved = incidentService.list(resolvedWrapper(date));

        IncidentEfficiencyMetrics metrics = new IncidentEfficiencyMetrics();
        metrics.setDate(date);
        metrics.setResolvedCount(resolved.size());

        List<Long> minutes = durations(resolved, Incident::getCreatedAt, Incident::getResolvedAt);
        metrics.setAvgResolutionMinutes(avg(minutes));
        metrics.setMinResolutionMinutes(min(minutes));
        metrics.setMaxResolutionMinutes(max(minutes));
        metrics.setByType(groupMinutes(resolved, Incident::getIncidentType));
        metrics.setBySeverity(groupMinutes(resolved, Incident::getSeverity));

        List<WorkOrder> completed = workOrderService.list(completedWrapper(date));
        metrics.setCompletedWorkOrderCount(completed.size());
        metrics.setAvgWorkOrderCompletionMinutes(
                avg(durations(completed, WorkOrder::getCreatedAt, WorkOrder::getCompletedAt)));

        return metrics;
    }

    @Override
    public AiSuggestionAdoptionMetrics getAiAdoptionRate(LocalDate date) {
        List<AiSuggestion> all = aiSuggestionService.list(
                date == null
                        ? Wrappers.<AiSuggestion>lambdaQuery()
                        : Wrappers.<AiSuggestion>lambdaQuery()
                                .ge(AiSuggestion::getCreatedAt, date.atStartOfDay())
                                .lt(AiSuggestion::getCreatedAt, date.plusDays(1).atStartOfDay()));

        long executed = count(all, "EXECUTED");
        long rejected = count(all, "REJECTED");
        long pending = count(all, "PENDING");

        AiSuggestionAdoptionMetrics metrics = new AiSuggestionAdoptionMetrics();
        metrics.setTotal(all.size());
        metrics.setExecuted((int) executed);
        metrics.setRejected((int) rejected);
        metrics.setPending((int) pending);
        metrics.setAdoptionRate(rate(executed, rejected));

        Map<String, List<AiSuggestion>> byType = all.stream()
                .collect(Collectors.groupingBy(s -> keyOrUnknown(s.getSuggestionType())));
        List<AiSuggestionAdoptionMetrics.TypeAdoption> types = byType.entrySet().stream()
                .map(e -> {
                    long ex = count(e.getValue(), "EXECUTED");
                    long rej = count(e.getValue(), "REJECTED");
                    long pend = count(e.getValue(), "PENDING");
                    AiSuggestionAdoptionMetrics.TypeAdoption t =
                            new AiSuggestionAdoptionMetrics.TypeAdoption();
                    t.setSuggestionType(e.getKey());
                    t.setExecuted((int) ex);
                    t.setRejected((int) rej);
                    t.setPending((int) pend);
                    t.setAdoptionRate(rate(ex, rej));
                    return t;
                })
                .sorted(Comparator.comparing(AiSuggestionAdoptionMetrics.TypeAdoption::getSuggestionType))
                .collect(Collectors.toList());
        metrics.setByType(types);
        return metrics;
    }

    // ---- 复用逻辑 ----

    /** 某天 resolved 的 incident；date 为空时统计全部 resolved。 */
    private LambdaQueryWrapper<Incident> resolvedWrapper(LocalDate date) {
        LambdaQueryWrapper<Incident> w = Wrappers.<Incident>lambdaQuery()
                .eq(Incident::getStatus, "RESOLVED")
                .isNotNull(Incident::getResolvedAt);
        if (date != null) {
            w.ge(Incident::getResolvedAt, date.atStartOfDay())
             .lt(Incident::getResolvedAt, date.plusDays(1).atStartOfDay());
        }
        return w;
    }

    /** 某天完成的工单；date 为空时统计全部完成。 */
    private LambdaQueryWrapper<WorkOrder> completedWrapper(LocalDate date) {
        LambdaQueryWrapper<WorkOrder> w = Wrappers.<WorkOrder>lambdaQuery()
                .eq(WorkOrder::getStatus, "COMPLETED")
                .isNotNull(WorkOrder::getCompletedAt);
        if (date != null) {
            w.ge(WorkOrder::getCompletedAt, date.atStartOfDay())
             .lt(WorkOrder::getCompletedAt, date.plusDays(1).atStartOfDay());
        }
        return w;
    }

    private long count(List<AiSuggestion> list, String status) {
        return list.stream().filter(s -> status.equals(s.getStatus())).count();
    }

    /** 采纳率 = EXECUTED / (EXECUTED + REJECTED) * 100%；分母为 0 时返回 null。 */
    private BigDecimal rate(long executed, long rejected) {
        long denom = executed + rejected;
        if (denom == 0) {
            return null;
        }
        return BigDecimal.valueOf(executed * 100.0 / denom).setScale(2, RoundingMode.HALF_UP);
    }

    private <T> List<Long> durations(
            List<T> rows, Function<T, LocalDateTime> from, Function<T, LocalDateTime> to) {
        return rows.stream()
                .filter(r -> from.apply(r) != null && to.apply(r) != null)
                .map(r -> Duration.between(from.apply(r), to.apply(r)).toMinutes())
                .collect(Collectors.toList());
    }

    private List<IncidentEfficiencyMetrics.GroupStat> groupMinutes(
            List<Incident> incidents, Function<Incident, String> keyFn) {
        Map<String, List<Incident>> grouped = incidents.stream()
                .collect(Collectors.groupingBy(i -> keyOrUnknown(keyFn.apply(i))));
        return grouped.entrySet().stream()
                .map(e -> {
                    IncidentEfficiencyMetrics.GroupStat g = new IncidentEfficiencyMetrics.GroupStat();
                    g.setKey(e.getKey());
                    g.setCount(e.getValue().size());
                    g.setAvgMinutes(avg(durations(
                            e.getValue(), Incident::getCreatedAt, Incident::getResolvedAt)));
                    return g;
                })
                .sorted(Comparator.comparing(IncidentEfficiencyMetrics.GroupStat::getKey))
                .collect(Collectors.toList());
    }

    private String keyOrUnknown(String key) {
        return key == null || key.isBlank() ? "UNKNOWN" : key;
    }

    private Long avg(List<Long> values) {
        if (values.isEmpty()) {
            return null;
        }
        double sum = values.stream().mapToLong(Long::longValue).sum();
        return Math.round(sum / values.size());
    }

    private Long min(List<Long> values) {
        return values.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    private Long max(List<Long> values) {
        return values.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
