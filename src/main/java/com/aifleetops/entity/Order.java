package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderCode;

    private String area;

    private Long vehicleId;

    private String status;

    private LocalDateTime createdTime;

    private LocalDateTime completedTime;

    private BigDecimal waitMinutes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
