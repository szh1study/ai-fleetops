package com.aifleetops.ai.service.impl;

import com.aifleetops.ai.dto.AiSuggestedAction;
import com.aifleetops.ai.executor.AiSuggestionExecutor;
import com.aifleetops.ai.service.AiSuggestionService;
import com.aifleetops.entity.AiSuggestion;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.mapper.AiSuggestionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * AI 操作建议服务实现。
 * <p>
 * 确认执行采用「原子状态抢占 + 单事务」：先把 PENDING 抢成 CONFIRMED（只有抢到的那次请求能继续），
 * 再在同一事务里执行动作；执行失败整体回滚，建议回到 PENDING 供重试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSuggestionServiceImpl extends ServiceImpl<AiSuggestionMapper, AiSuggestion> implements AiSuggestionService {

    private static final String CREATE_WORK_ORDER = "CREATE_WORK_ORDER";

    private final AiSuggestionExecutor aiSuggestionExecutor;

    @Override
    @Transactional
    public void persistSuggestions(Long incidentId, List<AiSuggestedAction> actions) {
        if (incidentId == null || actions == null || actions.isEmpty()) {
            return;
        }
        for (AiSuggestedAction action : actions) {
            if (action.getType() == null || action.getPayload() == null) {
                continue;
            }
            AiSuggestion suggestion = new AiSuggestion();
            suggestion.setIncidentId(incidentId);
            suggestion.setSuggestionType(action.getType().name());
            suggestion.setPayload(action.getPayload());
            suggestion.setReason(action.getReason());
            suggestion.setStatus("PENDING");
            save(suggestion);
            action.setSuggestionId(suggestion.getId());
        }
    }

    @Override
    public List<AiSuggestion> listByIncident(Long incidentId) {
        return lambdaQuery()
                .eq(AiSuggestion::getIncidentId, incidentId)
                .orderByDesc(AiSuggestion::getId)
                .list();
    }

    @Override
    @Transactional
    public AiSuggestion confirm(Long id, String confirmedBy, String description) {
        // 原子抢占：只有 PENDING 能转成 CONFIRMED，谁抢到谁执行，天然防重复提交。
        boolean claimed = lambdaUpdate()
                .eq(AiSuggestion::getId, id)
                .eq(AiSuggestion::getStatus, "PENDING")
                .set(AiSuggestion::getStatus, "CONFIRMED")
                .update();
        if (!claimed) {
            throw new BusinessException(409, "建议不存在或已处理");
        }

        AiSuggestion suggestion = getById(id);

        // 工单描述允许人工在确认时覆盖；仅对创建工单类动作生效。
        if (CREATE_WORK_ORDER.equals(suggestion.getSuggestionType())
                && suggestion.getPayload() != null
                && description != null && !description.isBlank()) {
            suggestion.getPayload().put("description", description.trim());
        }

        try {
            Map<String, Object> executedResult = aiSuggestionExecutor.execute(suggestion);
            suggestion.setExecutedResult(executedResult);
            suggestion.setStatus("EXECUTED");
            suggestion.setConfirmedBy(confirmedBy);
        } catch (BusinessException e) {
            // 业务失败：抛出后事务整体回滚，建议回到 PENDING，可重试。
            throw e;
        } catch (Exception e) {
            log.error("AI suggestion execution failed. id={}", id, e);
            throw new BusinessException(500, "建议执行失败，请稍后重试");
        }

        // 置空 updatedAt，让数据库的 ON UPDATE CURRENT_TIMESTAMP 自动维护，避免用旧值覆盖。
        suggestion.setUpdatedAt(null);
        updateById(suggestion);
        return getById(id);
    }

    @Override
    @Transactional
    public AiSuggestion reject(Long id) {
        boolean rejected = lambdaUpdate()
                .eq(AiSuggestion::getId, id)
                .eq(AiSuggestion::getStatus, "PENDING")
                .set(AiSuggestion::getStatus, "REJECTED")
                .update();
        if (!rejected) {
            throw new BusinessException(409, "建议不存在或已处理");
        }
        return getById(id);
    }
}
