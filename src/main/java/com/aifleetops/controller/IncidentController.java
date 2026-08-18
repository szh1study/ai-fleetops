package com.aifleetops.controller;

import com.aifleetops.ai.dto.IncidentAiAnalysisResponse;
import com.aifleetops.ai.service.IncidentAiAnalysisService;
import com.aifleetops.common.Result;
import com.aifleetops.dto.StatusUpdateDTO;
import com.aifleetops.entity.Incident;
import com.aifleetops.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentAiAnalysisService incidentAiAnalysisService;

    @GetMapping
    public Result<List<Incident>> list() {
        return Result.success(incidentService.list());
    }

    @GetMapping("/{id}")
    public Result<Incident> getById(@PathVariable Long id) {
        return Result.success(incidentService.getById(id));
    }

    @GetMapping("/active")
    public Result<List<Incident>> listActive() {
        return Result.success(incidentService.listActive());
    }

    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        incidentService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    /**
     * V0.2 AI Incident Copilot：后端根据 incidentId 自动读取业务上下文并执行 AI 分析。
     */
    @PostMapping("/{id}/ai-analysis")
    public Result<IncidentAiAnalysisResponse> analyzeIncident(@PathVariable Long id) {
        return Result.success(incidentAiAnalysisService.analyze(id));
    }
}
