package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 单个区域的订单量预测（V0.4.1）。
 */
@Data
public class AreaForecast implements Serializable {

    private static final long serialVersionUID = 1L;

    private String area;

    private Integer predictedOrders;

    /** 预测依据：引用历史均值与外部因素 */
    private String reason;
}
