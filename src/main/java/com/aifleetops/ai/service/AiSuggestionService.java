package com.aifleetops.ai.service;

import com.aifleetops.ai.dto.AiSuggestedAction;
import com.aifleetops.entity.AiSuggestion;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * AI 操作建议服务：生成、查询、确认执行与拒绝。
 * <p>
 * 生成发生在 AI 分析流程中；确认执行是 V0.3 的核心——所有动作都必须人工确认，
 * 系统不自动执行。
 */
public interface AiSuggestionService extends IService<AiSuggestion> {

    /**
     * 把 AI 分析返回的建议落库为 PENDING，并回填每个建议的 {@code suggestionId}。
     */
    void persistSuggestions(Long incidentId, List<AiSuggestedAction> actions);

    /**
     * 查询某个异常事件下的历史建议。
     */
    List<AiSuggestion> listByIncident(Long incidentId);

    /**
     * 确认并执行建议（幂等：仅 PENDING 可执行）。
     *
     * @param description 工单描述覆盖，仅 CREATE_WORK_ORDER 生效，可空
     */
    AiSuggestion confirm(Long id, String confirmedBy, String description);

    /**
     * 拒绝建议（幂等：仅 PENDING 可拒绝）。
     */
    AiSuggestion reject(Long id);
}
