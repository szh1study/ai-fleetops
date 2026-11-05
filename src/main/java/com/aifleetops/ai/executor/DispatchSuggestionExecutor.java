package com.aifleetops.ai.executor;

import com.aifleetops.entity.DispatchSuggestion;
import com.aifleetops.entity.Vehicle;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.service.VehicleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把已确认的调度建议执行为车辆区域调动（V0.4.2）。
 * <p>
 * 从 fromArea 取 count 辆 IDLE 车，更新 currentArea = toArea。
 * 注意：未做车辆级加锁，并发确认多条建议时可能超调；V0.4 单机小数据量下接受此限制。
 */
@Component
@RequiredArgsConstructor
public class DispatchSuggestionExecutor {

    private final VehicleService vehicleService;

    public Map<String, Object> execute(DispatchSuggestion suggestion) {
        String fromArea = suggestion.getFromArea();
        String toArea = suggestion.getToArea();
        int count = suggestion.getVehicleCount() == null ? 0 : suggestion.getVehicleCount();

        if (count <= 0) {
            throw new BusinessException(400, "调动车辆数必须大于 0");
        }
        if (fromArea == null || toArea == null || fromArea.equals(toArea)) {
            throw new BusinessException(400, "调出 / 调入区域无效");
        }

        List<Vehicle> candidates = vehicleService.list(
                Wrappers.<Vehicle>lambdaQuery()
                        .eq(Vehicle::getCurrentArea, fromArea)
                        .eq(Vehicle::getVehicleStatus, "IDLE")
                        .orderByAsc(Vehicle::getId));

        if (candidates.size() < count) {
            throw new BusinessException(409,
                    "调出区域可调度车辆不足，需要 " + count + " 辆，实际 " + candidates.size() + " 辆");
        }

        List<Long> movedIds = new ArrayList<>();
        for (Vehicle vehicle : candidates.subList(0, count)) {
            vehicle.setCurrentArea(toArea);
            vehicle.setUpdatedAt(null); // 让数据库 ON UPDATE 自动维护
            vehicleService.updateById(vehicle);
            movedIds.add(vehicle.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("movedVehicleIds", movedIds);
        result.put("count", movedIds.size());
        result.put("fromArea", fromArea);
        result.put("toArea", toArea);
        return result;
    }
}
