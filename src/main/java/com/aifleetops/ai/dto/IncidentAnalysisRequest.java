package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 发送给 AI Incident Copilot 的完整业务上下文。
 * <p>
 * 在 AI 数据流中作为请求体，由后端在异常发生后组装，
 * 携带车辆实时状态、设备状态、异常事件信息以及可选的 SOP 检索上下文，
 * 供大模型进行结构化异常分析。
 */
@Data
public class IncidentAnalysisRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========== 车辆实时状态 ==========

    /** 车辆主键 ID */
    private Long vehicleId;

    /** 车辆编号（如 R019） */
    private String vehicleCode;

    /** 车辆状态（READY / OPERATING / MAINTENANCE 等） */
    private String vehicleStatus;

    /** 订单状态（NONE / ASSIGNED / SERVING 等） */
    private String orderStatus;

    /** 电池电量百分比 */
    private Integer soc;

    /** 当前所在区域 */
    private String currentArea;

    // ========== 设备状态 ==========

    /** 摄像头状态 */
    private String cameraStatus;

    /** 激光雷达状态 */
    private String lidarStatus;

    /** 网络状态 */
    private String networkStatus;

    /** 软件状态 */
    private String softwareStatus;

    // ========== 异常事件信息 ==========

    /** 异常事件主键 ID */
    private Long incidentId;

    /** 异常类型（LOW_SOC / CAMERA_OFFLINE 等） */
    private String incidentType;

    /** 严重等级（LOW / MEDIUM / HIGH / CRITICAL） */
    private String severity;

    /** 异常描述 */
    private String description;

    /** 异常当前状态（OPEN / PROCESSING 等） */
    private String incidentStatus;

    /** 异常发生时间 */
    private LocalDateTime createdAt;

    // ========== 知识检索上下文 ==========

    /** 命中的 SOP 上下文列表（后续 RAG 阶段填充，当前可为空） */
    private List<SopContext> sopContexts;
}
