package com.aifleetops.ai.executor;

import com.aifleetops.entity.AiSuggestion;
import com.aifleetops.entity.Incident;
import com.aifleetops.entity.WorkOrder;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.service.IncidentService;
import com.aifleetops.service.VehicleService;
import com.aifleetops.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 把已确认的 AI 建议映射为真实业务动作。
 * <p>
 * 这里是 Human-in-the-loop 的执行边界：只有被人工确认后的建议才会走到这里。
 * 每个动作在落库前都会校验 payload 的合法值，不信任模型的自由输出。
 */
@Component
@RequiredArgsConstructor
public class AiSuggestionExecutor {

    private static final Set<String> WORK_ORDER_TYPES = Set.of(
            "CAMERA_INSPECTION", "LIDAR_INSPECTION", "CHARGING", "SOFTWARE_CHECK", "GENERAL_INSPECTION");
    private static final Set<String> INCIDENT_STATUSES = Set.of(
            "OPEN", "ANALYZING", "PENDING_APPROVAL", "PROCESSING", "RESOLVED", "REJECTED");
    private static final Set<String> VEHICLE_STATUSES = Set.of(
            "READY", "OPERATING", "IDLE", "CHARGING", "TO_CHARGE", "RETURNING", "MAINTENANCE", "OFFLINE");

    private final WorkOrderService workOrderService;
    private final IncidentService incidentService;
    private final VehicleService vehicleService;

    /**
     * 执行已确认的建议，返回执行结果（写入 executed_result）。
     */
    public Map<String, Object> execute(AiSuggestion suggestion) {
        String type = suggestion.getSuggestionType();
        if (type == null) {
            throw new BusinessException(400, "建议类型为空");
        }
        switch (type) {
            case "CREATE_WORK_ORDER":
                return executeCreateWorkOrder(suggestion);
            case "UPDATE_INCIDENT_STATUS":
                return executeUpdateIncidentStatus(suggestion);
            case "UPDATE_VEHICLE_STATUS":
                return executeUpdateVehicleStatus(suggestion);
            default:
                throw new BusinessException(400, "不支持的建议类型: " + type);
        }
    }

    private Map<String, Object> executeCreateWorkOrder(AiSuggestion suggestion) {
        Map<String, Object> payload = suggestion.getPayload();
        String workOrderType = asString(payload.get("workOrderType"));
        if (!WORK_ORDER_TYPES.contains(workOrderType)) {
            throw new BusinessException(400, "不支持的工单类型: " + workOrderType);
        }
        String description = asString(payload.get("description"));

        Incident incident = incidentService.getById(suggestion.getIncidentId());
        if (incident == null) {
            throw new BusinessException(404, "关联异常不存在: " + suggestion.getIncidentId());
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderCode(generateWorkOrderCode());
        workOrder.setVehicleId(incident.getVehicleId());
        workOrder.setIncidentId(incident.getId());
        workOrder.setType(workOrderType);
        workOrder.setDescription(description);
        workOrder.setStatus("PENDING");
        workOrderService.save(workOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("workOrderId", workOrder.getId());
        result.put("workOrderCode", workOrder.getWorkOrderCode());
        return result;
    }

    private Map<String, Object> executeUpdateIncidentStatus(AiSuggestion suggestion) {
        String status = asString(suggestion.getPayload().get("status"));
        if (!INCIDENT_STATUSES.contains(status)) {
            throw new BusinessException(400, "不支持的异常状态: " + status);
        }
        boolean updated = incidentService.updateStatus(suggestion.getIncidentId(), status);
        if (!updated) {
            throw new BusinessException(404, "关联异常不存在: " + suggestion.getIncidentId());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("incidentId", suggestion.getIncidentId());
        result.put("status", status);
        return result;
    }

    private Map<String, Object> executeUpdateVehicleStatus(AiSuggestion suggestion) {
        String status = asString(suggestion.getPayload().get("status"));
        if (!VEHICLE_STATUSES.contains(status)) {
            throw new BusinessException(400, "不支持的车辆状态: " + status);
        }
        Incident incident = incidentService.getById(suggestion.getIncidentId());
        if (incident == null) {
            throw new BusinessException(404, "关联异常不存在: " + suggestion.getIncidentId());
        }
        boolean updated = vehicleService.updateStatus(incident.getVehicleId(), status);
        if (!updated) {
            throw new BusinessException(404, "关联车辆不存在: " + incident.getVehicleId());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("vehicleId", incident.getVehicleId());
        result.put("status", status);
        return result;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String generateWorkOrderCode() {
        return "WO" + System.currentTimeMillis();
    }
}
