package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI 建议采纳率（V0.5.1）。
 * <p>
 * adoptionRate = EXECUTED / (EXECUTED + REJECTED) * 100%，已决策的建议中采纳占比；
 * PENDING 单独展示，不进入采纳率分母。
 */
@Data
public class AiSuggestionAdoptionMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer total;

    private Integer executed;

    private Integer rejected;

    private Integer pending;

    /** 采纳率（%），已决策数为 0 时为 null */
    private BigDecimal adoptionRate;

    /** 按建议类型分组 */
    private List<TypeAdoption> byType;

    @Data
    public static class TypeAdoption implements Serializable {

        private static final long serialVersionUID = 1L;

        private String suggestionType;

        private Integer executed;

        private Integer rejected;

        private Integer pending;

        private BigDecimal adoptionRate;
    }
}
