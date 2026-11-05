package com.aifleetops.ai.service.impl;

import com.aifleetops.ai.executor.DispatchSuggestionExecutor;
import com.aifleetops.ai.service.DispatchSuggestionService;
import com.aifleetops.entity.DispatchSuggestion;
import com.aifleetops.exception.BusinessException;
import com.aifleetops.mapper.DispatchSuggestionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 车辆调度建议服务实现（V0.4.2）。
 * <p>
 * 确认执行采用与 V0.3 相同的「原子状态抢占 + 单事务」：PENDING→CONFIRMED 抢占成功才执行，
 * 业务失败整体回滚，建议回到 PENDING 可重试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchSuggestionServiceImpl extends ServiceImpl<DispatchSuggestionMapper, DispatchSuggestion>
        implements DispatchSuggestionService {

    private final DispatchSuggestionExecutor dispatchSuggestionExecutor;

    @Override
    public List<DispatchSuggestion> listByQuery(LocalDate date, String status) {
        return lambdaQuery()
                .eq(date != null, DispatchSuggestion::getTargetDate, date)
                .eq(status != null && !status.isBlank(), DispatchSuggestion::getStatus, status)
                .orderByDesc(DispatchSuggestion::getId)
                .list();
    }

    @Override
    public void supersedePending(LocalDate date, String timeRange) {
        lambdaUpdate()
                .eq(DispatchSuggestion::getTargetDate, date)
                .eq(DispatchSuggestion::getTimeRange, timeRange)
                .eq(DispatchSuggestion::getStatus, "PENDING")
                .set(DispatchSuggestion::getStatus, "REJECTED")
                .update();
    }

    @Override
    @Transactional
    public DispatchSuggestion confirm(Long id, String confirmedBy) {
        boolean claimed = lambdaUpdate()
                .eq(DispatchSuggestion::getId, id)
                .eq(DispatchSuggestion::getStatus, "PENDING")
                .set(DispatchSuggestion::getStatus, "CONFIRMED")
                .update();
        if (!claimed) {
            throw new BusinessException(409, "建议不存在或已处理");
        }

        DispatchSuggestion suggestion = getById(id);
        try {
            Map<String, Object> executedResult = dispatchSuggestionExecutor.execute(suggestion);
            suggestion.setExecutedResult(executedResult);
            suggestion.setStatus("EXECUTED");
            suggestion.setConfirmedBy(confirmedBy);
        } catch (BusinessException e) {
            // 业务失败：事务整体回滚，建议回到 PENDING，可重试。
            throw e;
        } catch (Exception e) {
            log.error("Dispatch execution failed. id={}", id, e);
            throw new BusinessException(500, "调度执行失败，请稍后重试");
        }

        suggestion.setUpdatedAt(null);
        updateById(suggestion);
        return getById(id);
    }

    @Override
    @Transactional
    public DispatchSuggestion reject(Long id) {
        boolean rejected = lambdaUpdate()
                .eq(DispatchSuggestion::getId, id)
                .eq(DispatchSuggestion::getStatus, "PENDING")
                .set(DispatchSuggestion::getStatus, "REJECTED")
                .update();
        if (!rejected) {
            throw new BusinessException(409, "建议不存在或已处理");
        }
        return getById(id);
    }
}
