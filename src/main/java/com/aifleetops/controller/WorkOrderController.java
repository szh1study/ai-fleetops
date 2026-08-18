package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.dto.StatusUpdateDTO;
import com.aifleetops.entity.WorkOrder;
import com.aifleetops.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public Result<List<WorkOrder>> list() {
        return Result.success(workOrderService.list());
    }

    @GetMapping("/{id}")
    public Result<WorkOrder> getById(@PathVariable Long id) {
        return Result.success(workOrderService.getById(id));
    }

    @PostMapping
    public Result<WorkOrder> create(@RequestBody WorkOrder workOrder) {
        workOrderService.save(workOrder);
        return Result.success(workOrder);
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        workOrderService.updateStatus(id, dto.getStatus());
        return Result.success();
    }
}
