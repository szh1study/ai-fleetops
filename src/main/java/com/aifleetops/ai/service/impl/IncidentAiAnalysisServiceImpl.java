package com.aifleetops.ai.service.impl;

import com.aifleetops.ai.dto.IncidentAiAnalysisResponse;
import com.aifleetops.ai.dto.IncidentAnalysisRequest;
import com.aifleetops.ai.dto.IncidentAnalysisResult;
import com.aifleetops.ai.provider.AiModelProvider;
import com.aifleetops.ai.service.IncidentAiAnalysisService;
import com.aifleetops.entity.Incident;
import com.aifleetops.entity.Vehicle;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.service.IncidentService;
import com.aifleetops.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * V0.2 Incident Copilot 核心编排服务。
 * <p>
 * Controller 只传 incidentId；车辆与异常业务上下文统一在后端读取和组装，
 * 避免让前端自行拼 Prompt 或传入可被篡改的车辆状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentAiAnalysisServiceImpl implements IncidentAiAnalysisService {

    private final IncidentService incidentService;
    private final VehicleService vehicleService;
    private final AiModelProvider aiModelProvider;

    @Override
    public IncidentAiAnalysisResponse analyze(Long incidentId) {
        Incident incident = incidentService.getById(incidentId);
        if (incident == null) {
            throw new BusinessException(404, "异常事件不存在: " + incidentId);
        }

        Vehicle vehicle = vehicleService.getById(incident.getVehicleId());
        if (vehicle == null) {
            throw new BusinessException(404, "异常事件关联车辆不存在: " + incident.getVehicleId());
        }

        if (!aiModelProvider.isAvailable()) {
            return IncidentAiAnalysisResponse.unavailable(
                    incidentId,
                    aiModelProvider.getProviderName(),
                    "AI Incident Copilot 当前未启用；FleetOps 主业务仍可正常使用"
            );
        }

        IncidentAnalysisRequest request = buildRequest(incident, vehicle);

        try {
            IncidentAnalysisResult result = aiModelProvider.analyzeIncident(request);
            return IncidentAiAnalysisResponse.available(
                    incidentId,
                    aiModelProvider.getProviderName(),
                    result
            );
        } catch (RuntimeException e) {
            // AI 是辅助能力：模型故障不能拖垮 FleetOps 的 Incident 主业务。
            log.error("AI incident analysis failed. incidentId={}, provider={}",
                    incidentId, aiModelProvider.getProviderName(), e);
            return IncidentAiAnalysisResponse.unavailable(
                    incidentId,
                    aiModelProvider.getProviderName(),
                    "AI 模型调用失败，请稍后重试；FleetOps 主业务不受影响"
            );
        }
    }

    private IncidentAnalysisRequest buildRequest(Incident incident, Vehicle vehicle) {
        IncidentAnalysisRequest request = new IncidentAnalysisRequest();

        request.setVehicleId(vehicle.getId());
        request.setVehicleCode(vehicle.getVehicleCode());
        request.setVehicleStatus(vehicle.getVehicleStatus());
        request.setOrderStatus(vehicle.getOrderStatus());
        request.setSoc(vehicle.getSoc());
        request.setCurrentArea(vehicle.getCurrentArea());
        request.setCameraStatus(vehicle.getCameraStatus());
        request.setLidarStatus(vehicle.getLidarStatus());
        request.setNetworkStatus(vehicle.getNetworkStatus());
        request.setSoftwareStatus(vehicle.getSoftwareStatus());

        request.setIncidentId(incident.getId());
        request.setIncidentType(incident.getIncidentType());
        request.setSeverity(incident.getSeverity());
        request.setDescription(incident.getDescription());
        request.setIncidentStatus(incident.getStatus());
        request.setCreatedAt(incident.getCreatedAt());

        // V0.2 不执行 RAG，SOP 上下文保持为空；V0.3 再由检索层填充。
        request.setSopContexts(null);
        return request;
    }
}
