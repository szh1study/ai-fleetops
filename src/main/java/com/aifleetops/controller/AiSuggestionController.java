package com.aifleetops.controller;

import com.aifleetops.ai.service.AiSuggestionService;
import com.aifleetops.common.Result;
import com.aifleetops.dto.AiSuggestionConfirmDTO;
import com.aifleetops.entity.AiSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * V0.3 AI 建议操作接口。
 * <p>
 * 生成建议复用 {@code POST /api/incidents/{id}/ai-analysis}（分析时顺带落库），
 * 这里只暴露查询历史、确认执行、拒绝三个动作。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiSuggestionController {

    private final AiSuggestionService aiSuggestionService;

    @GetMapping("/incidents/{incidentId}/ai-suggestions")
    public Result<List<AiSuggestion>> listByIncident(@PathVariable Long incidentId) {
        return Result.success(aiSuggestionService.listByIncident(incidentId));
    }

    @PostMapping("/ai-suggestions/{id}/confirm")
    public Result<AiSuggestion> confirm(@PathVariable Long id,
                                        @RequestBody(required = false) AiSuggestionConfirmDTO dto) {
        String confirmedBy = dto == null ? null : dto.getConfirmedBy();
        String description = dto == null ? null : dto.getDescription();
        return Result.success(aiSuggestionService.confirm(id, confirmedBy, description));
    }

    @PostMapping("/ai-suggestions/{id}/reject")
    public Result<AiSuggestion> reject(@PathVariable Long id) {
        return Result.success(aiSuggestionService.reject(id));
    }
}
