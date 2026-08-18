package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Incident Copilot API 对前端返回的统一响应结构。
 * <p>
 * aiAvailable=false 表示本次没有得到有效的 AI 分析结果；此时 analysis 必须为 null，
 * 避免把“AI 未启用/调用失败”错误表达成 LOW 风险等业务结论。
 */
@Data
public class IncidentAiAnalysisResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前分析对应的异常事件 ID */
    private Long incidentId;

    /** 本次是否成功获得 AI 分析结果 */
    private Boolean aiAvailable;

    /** 当前配置的模型供应商标识 */
    private String provider;

    /** 面向前端的状态说明 */
    private String message;

    /** AI 分析结果；aiAvailable=false 时为 null */
    private IncidentAnalysisResult analysis;

    public static IncidentAiAnalysisResponse available(Long incidentId,
                                                       String provider,
                                                       IncidentAnalysisResult analysis) {
        IncidentAiAnalysisResponse response = new IncidentAiAnalysisResponse();
        response.setIncidentId(incidentId);
        response.setAiAvailable(true);
        response.setProvider(provider);
        response.setMessage("AI 分析完成");
        response.setAnalysis(analysis);
        return response;
    }

    public static IncidentAiAnalysisResponse unavailable(Long incidentId,
                                                         String provider,
                                                         String message) {
        IncidentAiAnalysisResponse response = new IncidentAiAnalysisResponse();
        response.setIncidentId(incidentId);
        response.setAiAvailable(false);
        response.setProvider(provider);
        response.setMessage(message);
        response.setAnalysis(null);
        return response;
    }
}
