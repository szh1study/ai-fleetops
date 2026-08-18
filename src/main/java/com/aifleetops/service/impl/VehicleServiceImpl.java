package com.aifleetops.service.impl;

import com.aifleetops.entity.Vehicle;
import com.aifleetops.enums.DeviceType;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.mapper.VehicleMapper;
import com.aifleetops.service.VehicleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    private static final String NORMAL = "NORMAL";

    @Override
    public List<Vehicle> listReady() {
        return lambdaQuery().eq(Vehicle::getVehicleStatus, "READY").list();
    }

    @Override
    public List<Vehicle> listAbnormal() {
        return lambdaQuery()
                .and(q -> q
                        .in(Vehicle::getVehicleStatus, Arrays.asList("MAINTENANCE", "OFFLINE"))
                        .or().ne(Vehicle::getCameraStatus, NORMAL)
                        .or().ne(Vehicle::getLidarStatus, NORMAL)
                        .or().ne(Vehicle::getNetworkStatus, NORMAL)
                        .or().ne(Vehicle::getSoftwareStatus, NORMAL))
                .list();
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return lambdaUpdate()
                .eq(Vehicle::getId, id)
                .set(Vehicle::getVehicleStatus, status)
                .update();
    }

    @Override
    public boolean updateDeviceStatus(Long id, DeviceType deviceType, String status) {
        var update = lambdaUpdate().eq(Vehicle::getId, id);
        switch (deviceType) {
            case CAMERA -> update.set(Vehicle::getCameraStatus, status);
            case LIDAR -> update.set(Vehicle::getLidarStatus, status);
            case NETWORK -> update.set(Vehicle::getNetworkStatus, status);
            case SOFTWARE -> update.set(Vehicle::getSoftwareStatus, status);
            default -> throw new BusinessException("不支持的设备类型: " + deviceType);
        }
        return update.update();
    }
}
