package com.aifleetops.ai.service;

import com.aifleetops.entity.DispatchSuggestion;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

/**
 * 车辆调度建议服务：查询、确认执行、拒绝（V0.4.2）。
 * <p>
 * 复用 V0.3 的状态机：确认 = CAS PENDING→CONFIRMED + 执行器，失败回滚到 PENDING。
 */
public interface DispatchSuggestionService extends IService<DispatchSuggestion> {

    List<DispatchSuggestion> listByQuery(LocalDate date, String status);

    /**
     * 把同 (targetDate, timeRange) 的旧 PENDING 建议标记为 REJECTED（被新一轮生成取代），避免堆积。
     */
    void supersedePending(LocalDate date, String timeRange);

    DispatchSuggestion confirm(Long id, String confirmedBy);

    DispatchSuggestion reject(Long id);
}
