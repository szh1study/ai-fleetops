package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("work_orders")
public class WorkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String workOrderCode;

    private Long vehicleId;

    private Long incidentId;

    private String type;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;
}
