package com.aifleetops.ai.provider.impl;

import com.aifleetops.ai.dto.IncidentAnalysisRequest;
import com.aifleetops.ai.dto.IncidentAnalysisResult;
import com.aifleetops.ai.provider.AiModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AI 未启用时的兜底 Provider。
 * <p>
 * 它只负责保证业务层始终可以注入 AiModelProvider，不伪造任何风险等级或分析结论。
 * 上层 IncidentAiAnalysisService 会通过 isAvailable() 判断并返回明确的不可用状态。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "ai.model.enabled", havingValue = "false", matchIfMissing = true)
public class NoopAiModelProvider implements AiModelProvider {

    private final String providerName;

    public NoopAiModelProvider(@Value("${ai.model.provider:disabled}") String providerName) {
        this.providerName = providerName;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public IncidentAnalysisResult analyzeIncident(IncidentAnalysisRequest request) {
        log.warn("AI model is disabled; analyzeIncident should not be invoked. incidentId={}",
                request == null ? null : request.getIncidentId());
        throw new IllegalStateException("AI Incident Copilot 当前未启用");
    }
}
