package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 车辆调度建议记录（V0.4.2）。
 * <p>
 * 由调度规则引擎计算，经人工确认后由 DispatchSuggestionExecutor 执行车辆区域调动。
 */
@Data
@TableName(value = "dispatch_suggestions", autoResultMap = true)
public class DispatchSuggestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 daily_forecasts 行（to_area 的预测行），可空 */
    private Long forecastId;

    private LocalDate targetDate;

    private String timeRange;

    private String fromArea;

    private String toArea;

    private Integer vehicleCount;

    private String reason;

    /** PENDING / EXECUTED / REJECTED（CONFIRMED 为事务内瞬态，不落库） */
    private String status;

    private String confirmedBy;

    /** 执行结果，如 {movedVehicleIds: [1, 3]} */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> executedResult;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
