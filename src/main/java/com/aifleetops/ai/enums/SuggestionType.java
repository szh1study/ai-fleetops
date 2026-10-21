package com.aifleetops.ai.enums;

/**
 * AI 建议动作的可执行类型。
 * <p>
 * V0.3 把 V0.2 里细粒度的动作枚举收敛为三类可执行动作，
 * 具体参数（如工单类型、目标状态）由 payload 承载。
 * 一次异常可返回多个建议，每个建议对应一种类型。
 */
public enum SuggestionType {
    /** 创建维修工单 */
    CREATE_WORK_ORDER,
    /** 更新异常事件状态 */
    UPDATE_INCIDENT_STATUS,
    /** 更新车辆状态 */
    UPDATE_VEHICLE_STATUS
}
