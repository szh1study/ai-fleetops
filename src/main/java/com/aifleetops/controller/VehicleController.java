package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.dto.DeviceStatusUpdateDTO;
import com.aifleetops.dto.StatusUpdateDTO;
import com.aifleetops.entity.Vehicle;
import com.aifleetops.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public Result<List<Vehicle>> list() {
        return Result.success(vehicleService.list());
    }

    @GetMapping("/{id}")
    public Result<Vehicle> getById(@PathVariable Long id) {
        return Result.success(vehicleService.getById(id));
    }

    @PostMapping
    public Result<Vehicle> create(@RequestBody Vehicle vehicle) {
        vehicleService.save(vehicle);
        return Result.success(vehicle);
    }

    @PutMapping("/{id}")
    public Result<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        vehicle.setId(id);
        vehicleService.updateById(vehicle);
        return Result.success(vehicle);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        vehicleService.removeById(id);
        return Result.success();
    }

    @GetMapping("/ready")
    public Result<List<Vehicle>> listReady() {
        return Result.success(vehicleService.listReady());
    }

    @GetMapping("/abnormal")
    public Result<List<Vehicle>> listAbnormal() {
        return Result.success(vehicleService.listAbnormal());
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        vehicleService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @PatchMapping("/{id}/device-status")
    public Result<Void> updateDeviceStatus(@PathVariable Long id, @Valid @RequestBody DeviceStatusUpdateDTO dto) {
        vehicleService.updateDeviceStatus(id, dto.getDeviceType(), dto.getStatus());
        return Result.success();
    }
}
