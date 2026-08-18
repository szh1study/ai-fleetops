package com.aifleetops.ai.service;

import com.aifleetops.ai.dto.IncidentAiAnalysisResponse;

/**
 * Incident Copilot 业务服务。
 */
public interface IncidentAiAnalysisService {

    /**
     * 读取 Incident 与 Vehicle 数据，组装完整上下文并请求 AI Provider 分析。
     */
    IncidentAiAnalysisResponse analyze(Long incidentId);
}
