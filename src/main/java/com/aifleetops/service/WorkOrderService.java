package com.aifleetops.service;

import com.aifleetops.entity.WorkOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface WorkOrderService extends IService<WorkOrder> {

    boolean updateStatus(Long id, String status);
}
