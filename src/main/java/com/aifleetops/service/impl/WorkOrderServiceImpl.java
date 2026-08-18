package com.aifleetops.service.impl;

import com.aifleetops.entity.WorkOrder;
import com.aifleetops.mapper.WorkOrderMapper;
import com.aifleetops.service.WorkOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    @Override
    public boolean updateStatus(Long id, String status) {
        var update = lambdaUpdate()
                .eq(WorkOrder::getId, id)
                .set(WorkOrder::getStatus, status);
        // 状态置为 COMPLETED 时自动记录完成时间，其他状态不覆盖已有 completedAt
        if ("COMPLETED".equals(status)) {
            update.set(WorkOrder::getCompletedAt, LocalDateTime.now());
        }
        return update.update();
    }
}
