package com.aifleetops.controller;

import com.aifleetops.ai.service.DispatchSuggestionService;
import com.aifleetops.common.Result;
import com.aifleetops.entity.DispatchSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * V0.4.2 车辆调度建议接口。
 * <p>
 * 生成调度建议复用 {@code POST /api/demand-forecasts/generate}（预测时顺带规则计算并落库），
 * 这里只暴露查询、确认执行、拒绝。
 */
@RestController
@RequestMapping("/api/dispatch-suggestions")
@RequiredArgsConstructor
public class DispatchSuggestionController {

    private final DispatchSuggestionService dispatchSuggestionService;

    @GetMapping
    public Result<List<DispatchSuggestion>> list(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status) {
        return Result.success(dispatchSuggestionService.listByQuery(date, status));
    }

    @PostMapping("/{id}/confirm")
    public Result<DispatchSuggestion> confirm(@PathVariable Long id,
                                              @RequestBody(required = false) Map<String, String> body) {
        String confirmedBy = body == null ? null : body.get("confirmedBy");
        return Result.success(dispatchSuggestionService.confirm(id, confirmedBy));
    }

    @PostMapping("/{id}/reject")
    public Result<DispatchSuggestion> reject(@PathVariable Long id) {
        return Result.success(dispatchSuggestionService.reject(id));
    }
}
