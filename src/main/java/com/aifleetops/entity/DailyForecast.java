package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_forecasts")
public class DailyForecast implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate forecastDate;

    private String area;

    private String timeRange;

    private Integer predictedOrders;

    private String predictionReason;

    private String weatherSummary;

    private LocalDateTime createdAt;
}
