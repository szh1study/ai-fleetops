package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("vehicles")
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vehicleCode;

    private String vehicleModel;

    private Integer soc;

    private String currentArea;

    private String vehicleStatus;

    private String orderStatus;

    private String cameraStatus;

    private String lidarStatus;

    private String networkStatus;

    private String softwareStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
