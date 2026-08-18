package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.entity.OperationReview;
import com.aifleetops.service.OperationReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operation-reviews")
@RequiredArgsConstructor
public class OperationReviewController {

    private final OperationReviewService operationReviewService;

    @GetMapping
    public Result<List<OperationReview>> list() {
        return Result.success(operationReviewService.list());
    }

    @GetMapping("/{id}")
    public Result<OperationReview> getById(@PathVariable Long id) {
        return Result.success(operationReviewService.getById(id));
    }

    @PostMapping
    public Result<OperationReview> create(@RequestBody OperationReview operationReview) {
        operationReviewService.save(operationReview);
        return Result.success(operationReview);
    }

    @PutMapping("/{id}")
    public Result<OperationReview> update(@PathVariable Long id, @RequestBody OperationReview operationReview) {
        operationReview.setId(id);
        operationReviewService.updateById(operationReview);
        return Result.success(operationReview);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        operationReviewService.removeById(id);
        return Result.success();
    }
}
