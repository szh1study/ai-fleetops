package com.aifleetops.ai.dto;

import com.aifleetops.ai.enums.RiskLevel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI Incident Copilot 必须返回的结构化异常分析结果。
 * <p>
 * 在 AI 数据流中作为响应体，约束大模型以固定结构输出分析结论，
 * 便于后端程序化解析与后续审批流转。
 * 其中 suggestedActions 为列表，因为一次异常处置可能需要多个业务动作；
 * requiresHumanApproval 标识该结果是否必须经管理员审批后方可执行。
 */
@Data
public class IncidentAnalysisResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 风险等级 */
    private RiskLevel riskLevel;

    /** 异常摘要 */
    private String summary;

    /** 可能的原因列表 */
    private List<String> possibleCauses;

    /** 处置建议列表 */
    private List<String> recommendations;

    /** 建议执行的业务动作列表（可能多个） */
    private List<AiSuggestedAction> suggestedActions;

    /** 模型置信度（0~1） */
    private Double confidence;

    /** 引用的 SOP 编号列表 */
    private List<String> sopReferences;

    /** 是否需要人工审批后方可执行 */
    private Boolean requiresHumanApproval;
}
