package com.aifleetops.ai.provider.impl;

import com.aifleetops.ai.dto.IncidentAnalysisRequest;
import com.aifleetops.ai.dto.IncidentAnalysisResult;
import com.aifleetops.ai.provider.AiModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

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
