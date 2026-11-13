package com.aifleetops.ai.provider;

import com.aifleetops.ai.dto.DailyReportRequest;
import com.aifleetops.ai.dto.DemandForecastRequest;
import com.aifleetops.ai.dto.DemandForecastResult;
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

    /**
     * 预测各区域未来时段的订单需求。
     *
     * @param request 携带目标日期、时段、区域、历史均值与可调度车辆数的上下文
     * @return 各区域订单量预测（不含调度建议，调度由规则计算）
     */
    DemandForecastResult predictDemand(DemandForecastRequest request);

    /**
     * 生成某天的运营日报文字摘要。
     *
     * @param request 携带当日复盘指标（预测偏差、调度效果、异常时效、AI 采纳率）
     * @return 自然语言日报正文
     */
    String generateDailyReport(DailyReportRequest request);
}
