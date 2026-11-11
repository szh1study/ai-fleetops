package com.aifleetops.service;

import com.aifleetops.dto.DispatchReviewItem;
import com.aifleetops.dto.ForecastReviewItem;
import com.aifleetops.entity.OperationReview;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

public interface OperationReviewService extends IService<OperationReview> {

    /**
     * 预测复盘：对比 daily_forecasts 预测订单量与 orders 实际下单量，计算偏差。
     */
    List<ForecastReviewItem> listForecastReview(LocalDate date);

    /**
     * 调度效果复盘：评估 dispatch_suggestions 目标区域的实际需求是否被满足。
     */
    List<DispatchReviewItem> listDispatchReview(LocalDate date);
}
