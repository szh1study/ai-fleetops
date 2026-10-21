package com.aifleetops.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 操作建议记录（V0.3）。
 * <p>
 * AI 分析 Incident 后生成可执行建议，先以 PENDING 落库；
 * 运营人员确认后由 {@code AiSuggestionExecutor} 执行，成功置 EXECUTED，拒绝置 REJECTED。
 * 该表同时为 V0.5 的「建议采纳率复盘」留存数据。
 */
@Data
@TableName(value = "ai_suggestions", autoResultMap = true)
public class AiSuggestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联异常事件 */
    private Long incidentId;

    /** CREATE_WORK_ORDER / UPDATE_INCIDENT_STATUS / UPDATE_VEHICLE_STATUS */
    private String suggestionType;

    /** 预填充数据（JSON） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    /** AI 给出建议的理由 */
    private String reason;

    /** PENDING / EXECUTED / REJECTED（CONFIRMED 为事务内瞬态，不落库） */
    private String status;

    /** 确认 / 拒绝的操作人，自由文本，可空 */
    private String confirmedBy;

    /** 执行结果（JSON），如 {workOrderId: 123} */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> executedResult;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
