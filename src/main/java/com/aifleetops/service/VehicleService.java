package com.aifleetops.service;

import com.aifleetops.entity.Vehicle;
import com.aifleetops.enums.DeviceType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VehicleService extends IService<Vehicle> {

    List<Vehicle> listReady();

    List<Vehicle> listAbnormal();

    boolean updateStatus(Long id, String status);

    boolean updateDeviceStatus(Long id, DeviceType deviceType, String status);
}
