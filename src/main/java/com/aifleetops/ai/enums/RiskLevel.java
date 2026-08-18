package com.aifleetops.ai.enums;

/**
 * AI Incident Copilot 输出的风险等级枚举。
 * <p>
 * 在 AI 数据流中，用于标识一次异常事件对车队运营的威胁程度，
 * 是 {@code IncidentAnalysisResult} 的必填结构化字段之一。
 * 后续 Prompt 会要求大模型必须从这四个值中选定一个返回。
 */
public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
