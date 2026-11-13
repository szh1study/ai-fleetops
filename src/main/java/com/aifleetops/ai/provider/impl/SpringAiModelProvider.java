package com.aifleetops.ai.provider.impl;

import com.aifleetops.ai.dto.DailyReportRequest;
import com.aifleetops.ai.dto.DemandForecastRequest;
import com.aifleetops.ai.dto.DemandForecastResult;
import com.aifleetops.ai.dto.IncidentAnalysisRequest;
import com.aifleetops.ai.dto.IncidentAnalysisResult;
import com.aifleetops.ai.provider.AiModelProvider;
import com.aifleetops.dto.AiSuggestionAdoptionMetrics;
import com.aifleetops.dto.IncidentEfficiencyMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 Spring AI ChatClient 的真实模型 Provider。
 * <p>
 * 业务层只依赖 AiModelProvider。本类负责把 FleetOps 上下文转成 Prompt，
 * 再通过 Spring AI Structured Output 将模型响应映射为 IncidentAnalysisResult。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.model.enabled", havingValue = "true")
public class SpringAiModelProvider implements AiModelProvider {

    private static final String SYSTEM_PROMPT = """
            你是 AI FleetOps 的 Incident Copilot，负责分析 Robotaxi 运营异常。
            只能依据输入的车辆状态、设备状态和 Incident 信息进行判断，不得虚构未提供的事实。

            输出要求：
            1. riskLevel 只能是 LOW、MEDIUM、HIGH、CRITICAL 之一。
            2. summary 用简洁中文概括异常及运营影响。
            3. possibleCauses 给出基于现有信息的可能原因；不确定时明确使用“可能”。
            4. recommendations 给出可执行但不自动执行的人工处置建议。
            5. suggestedActions 每个元素必须包含 type、payload、reason 三个字段：
               - type 只能是 CREATE_WORK_ORDER、UPDATE_INCIDENT_STATUS、UPDATE_VEHICLE_STATUS 之一。
               - CREATE_WORK_ORDER 的 payload.workOrderType 只能是 CAMERA_INSPECTION、LIDAR_INSPECTION、CHARGING、SOFTWARE_CHECK、GENERAL_INSPECTION 之一；payload.description 必须基于给定的车辆/设备状态生成，不得虚构。
               - UPDATE_INCIDENT_STATUS 的 payload.status 只能是 OPEN、ANALYZING、PENDING_APPROVAL、PROCESSING、RESOLVED、REJECTED 之一。
               - UPDATE_VEHICLE_STATUS 的 payload.status 只能是 READY、OPERATING、IDLE、CHARGING、TO_CHARGE、RETURNING、MAINTENANCE、OFFLINE 之一。
               - reason 必须说明为什么给出这个建议，依据来自输入信息。
            6. confidence 必须在 0 到 1 之间。
            7. V0.3 尚未启用 RAG，sopReferences 返回空列表。
            8. 任何建议动作都只是 Copilot 建议，系统不会自动执行，必须人工确认后才会执行。
            """;

    private static final String DEMAND_SYSTEM_PROMPT = """
            你是 AI FleetOps 的 Demand Forecast Copilot，负责预测 Robotaxi 各区域未来时段的订单需求。
            只能依据输入的历史均值与外部因素进行判断，不得虚构未提供的外部因素。

            输出要求：
            1. forecasts 只对输入 areas 中出现的区域各输出一项，不得新增区域。
            2. predictedOrders 为非负整数，以 historicalAvg 为基准，结合 externalFactors 合理上调或下调。
            3. reason 必须用中文，引用历史均值与给定的外部因素解释预测依据，不得虚构未提供的外部因素。
            4. 不要输出任何调度建议；调度由后端规则计算。
            """;

    private static final String DAILY_REPORT_SYSTEM_PROMPT = """
            你是 AI FleetOps 的运营日报助手，负责把当天的运营复盘指标总结成一段简洁中文日报。
            只能依据输入的指标生成，不得虚构输入中未体现的数据。

            输出要求：
            1. 用简洁中文输出，控制在 200 字以内。
            2. 指出最重要的 1-2 个结论（例如哪个区域预测偏差最大、调度是否有效）。
            3. 对缺失或暂无数据的指标，用「暂无数据」一带而过，不要编造。
            4. 只输出日报正文本身，不要加标题、编号或 Markdown 标记。
            """;

    private final ChatClient chatClient;
    private final String providerName;

    public SpringAiModelProvider(ChatClient.Builder chatClientBuilder,
                                 @Value("${ai.model.provider:unknown}") String providerName) {
        this.chatClient = chatClientBuilder.build();
        this.providerName = providerName;
        log.info("SpringAiModelProvider initialized, provider={}", providerName);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public IncidentAnalysisResult analyzeIncident(IncidentAnalysisRequest request) {
        String userPrompt = buildIncidentPrompt(request);

        IncidentAnalysisResult result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .entity(IncidentAnalysisResult.class);

        validateAndNormalize(result);
        log.info("AI incident analysis completed. incidentId={}, provider={}, riskLevel={}",
                request.getIncidentId(), providerName, result.getRiskLevel());
        return result;
    }

    @Override
    public DemandForecastResult predictDemand(DemandForecastRequest request) {
        String userPrompt = buildDemandPrompt(request);

        DemandForecastResult result = chatClient.prompt()
                .system(DEMAND_SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .entity(DemandForecastResult.class);

        normalizeDemandResult(result);
        log.info("AI demand forecast completed. targetDate={}, timeRange={}, areas={}",
                request.getTargetDate(), request.getTimeRange(), request.getAreas());
        return result;
    }

    @Override
    public String generateDailyReport(DailyReportRequest request) {
        String userPrompt = buildDailyReportPrompt(request);

        String content = chatClient.prompt()
                .system(DAILY_REPORT_SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI 模型返回了空日报");
        }
        log.info("AI daily report generated. reportDate={}, provider={}",
                request.getReportDate(), providerName);
        return content.trim();
    }

    private String buildDailyReportPrompt(DailyReportRequest request) {
        String forecastLines = request.getForecastReviews() == null || request.getForecastReviews().isEmpty()
                ? "暂无预测复盘数据"
                : request.getForecastReviews().stream()
                        .map(f -> String.format("%s %s：预测 %s / 实际 %s（偏差 %s）",
                                value(f.getArea()), value(f.getTimeRange()),
                                value(f.getPredictedOrders()), value(f.getActualOrders()),
                                f.getDeviation() == null ? "-" : f.getDeviation() + "%"))
                        .collect(Collectors.joining("；"));

        String dispatchLines = request.getDispatchReviews() == null || request.getDispatchReviews().isEmpty()
                ? "暂无调度建议"
                : request.getDispatchReviews().stream()
                        .map(d -> String.format("从 %s 调 %s 辆到 %s（%s）",
                                value(d.getFromArea()), value(d.getVehicleCount()),
                                value(d.getToArea()), value(d.getStatus())))
                        .collect(Collectors.joining("；"));

        IncidentEfficiencyMetrics im = request.getIncidentMetrics();
        AiSuggestionAdoptionMetrics aa = request.getAiAdoption();

        return """
                请生成以下日期的运营日报：

                复盘日期：%s

                [预测偏差] 整体偏差 %s
                %s

                [调度效果] %s

                [异常时效] 当日解决 %s 条，平均解决耗时 %s 分钟
                [AI 建议采纳率] 累计采纳率 %s，已执行 %s / 已拒绝 %s / 待处理 %s

                请输出日报正文。
                """.formatted(
                value(request.getReportDate()),
                request.getOverallDeviation() == null ? "-" : request.getOverallDeviation() + "%",
                forecastLines,
                dispatchLines,
                value(im == null ? null : im.getResolvedCount()),
                value(im == null ? null : im.getAvgResolutionMinutes()),
                value(aa == null ? null : aa.getAdoptionRate()),
                value(aa == null ? null : aa.getExecuted()),
                value(aa == null ? null : aa.getRejected()),
                value(aa == null ? null : aa.getPending())
        );
    }

    private String buildDemandPrompt(DemandForecastRequest request) {
        return """
                请预测以下区域在未来时段的订单需求：

                目标日期：%s
                时段：%s
                区域：%s
                外部因素：%s
                历史同期平均订单：%s
                当前各区域可调度车辆数：%s

                请输出结构化 DemandForecastResult。
                """.formatted(
                value(request.getTargetDate()),
                value(request.getTimeRange()),
                request.getAreas(),
                request.getExternalFactors(),
                request.getHistoricalAvg(),
                request.getCurrentIdleVehicles()
        );
    }

    private void normalizeDemandResult(DemandForecastResult result) {
        if (result == null) {
            throw new IllegalStateException("AI 模型返回了空预测结果");
        }
        if (result.getForecasts() == null) {
            result.setForecasts(List.of());
        } else {
            result.setForecasts(result.getForecasts().stream()
                    .filter(f -> f != null && f.getArea() != null && f.getPredictedOrders() != null)
                    .toList());
        }
    }

    private String buildIncidentPrompt(IncidentAnalysisRequest request) {
        return """
                请分析以下 Robotaxi 异常事件：

                [车辆]
                vehicleId: %s
                vehicleCode: %s
                vehicleStatus: %s
                orderStatus: %s
                soc: %s
                currentArea: %s

                [设备]
                cameraStatus: %s
                lidarStatus: %s
                networkStatus: %s
                softwareStatus: %s

                [Incident]
                incidentId: %s
                incidentType: %s
                severity: %s
                description: %s
                incidentStatus: %s
                createdAt: %s

                当前版本没有 SOP/RAG 上下文。请输出结构化 IncidentAnalysisResult。
                """.formatted(
                value(request.getVehicleId()),
                value(request.getVehicleCode()),
                value(request.getVehicleStatus()),
                value(request.getOrderStatus()),
                value(request.getSoc()),
                value(request.getCurrentArea()),
                value(request.getCameraStatus()),
                value(request.getLidarStatus()),
                value(request.getNetworkStatus()),
                value(request.getSoftwareStatus()),
                value(request.getIncidentId()),
                value(request.getIncidentType()),
                value(request.getSeverity()),
                value(request.getDescription()),
                value(request.getIncidentStatus()),
                value(request.getCreatedAt())
        );
    }

    private String value(Object value) {
        return value == null ? "UNKNOWN" : String.valueOf(value);
    }

    private void validateAndNormalize(IncidentAnalysisResult result) {
        if (result == null) {
            throw new IllegalStateException("AI 模型返回了空分析结果");
        }
        if (result.getRiskLevel() == null) {
            throw new IllegalStateException("AI 分析结果缺少 riskLevel");
        }
        if (result.getConfidence() != null
                && (result.getConfidence() < 0.0 || result.getConfidence() > 1.0)) {
            throw new IllegalStateException("AI 分析结果 confidence 超出 0~1 范围");
        }

        if (result.getPossibleCauses() == null) {
            result.setPossibleCauses(List.of());
        }
        if (result.getRecommendations() == null) {
            result.setRecommendations(List.of());
        }
        if (result.getSuggestedActions() == null) {
            result.setSuggestedActions(List.of());
        } else {
            result.setSuggestedActions(result.getSuggestedActions().stream()
                    .filter(action -> action != null && action.getType() != null && action.getPayload() != null)
                    .toList());
        }
        if (result.getSopReferences() == null) {
            result.setSopReferences(List.of());
        }
        if (result.getRequiresHumanApproval() == null) {
            result.setRequiresHumanApproval(Boolean.TRUE);
        }
    }
}
