package com.aifleetops.ai.enums;

/**
 * AI Incident Copilot 建议执行的业务动作枚举。
 * <p>
 * 在 AI 数据流中，表示大模型针对一次异常给出的处置动作集合。
 * 一次异常可能需要多个动作（例如先暂停派单、再创建检修工单），
 * 因此 {@code IncidentAnalysisResult.suggestedActions} 使用 List 承载。
 * 后续这些动作需经管理员审批后才由系统真正执行。
 */
public enum SuggestedAction {
    /** 无需动作 */
    NO_ACTION,

    /** 暂停向该车辆派发新订单 */
    STOP_DISPATCH,
    /** 保持维修状态，不恢复运营 */
    KEEP_MAINTENANCE,

    /** 前往充电站补能 */
    SEND_TO_CHARGE,
    /** 返场 */
    RETURN_TO_STATION,

    /** 创建摄像头检修工单 */
    CREATE_CAMERA_INSPECTION_WORK_ORDER,
    /** 创建激光雷达检修工单 */
    CREATE_LIDAR_INSPECTION_WORK_ORDER,
    /** 创建软件检查工单 */
    CREATE_SOFTWARE_CHECK_WORK_ORDER,
    /** 创建常规检查工单 */
    CREATE_GENERAL_INSPECTION_WORK_ORDER
}
