package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("incidents")
public class Incident implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private String incidentType;

    private String severity;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime updatedAt;
}
