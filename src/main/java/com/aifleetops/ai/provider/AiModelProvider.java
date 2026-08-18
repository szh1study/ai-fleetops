package com.aifleetops.ai.provider;

import com.aifleetops.ai.dto.IncidentAnalysisRequest;
import com.aifleetops.ai.dto.IncidentAnalysisResult;

/**
 * AI 模型业务抽象接口。
 * <p>
 * AI FleetOps 的业务 Service 仅依赖此接口，不直接耦合 Qwen、DeepSeek 或 Spring AI 等具体实现。
 * 后续切换模型供应商时，只需替换/新增 Provider 实现，无需修改 Incident Copilot 业务层。
 */
public interface AiModelProvider {

    /**
     * 当前 Provider 是否能够执行真实模型调用。
     */
    boolean isAvailable();

    /**
     * 返回当前 Provider 的业务标识，例如 qwen / deepseek。
     */
    String getProviderName();

    /**
     * 对异常事件进行 AI 分析。
     *
     * @param request 携带车辆、设备、异常等完整上下文的请求体
     * @return 结构化的异常分析结果
     */
    IncidentAnalysisResult analyzeIncident(IncidentAnalysisRequest request);
}
