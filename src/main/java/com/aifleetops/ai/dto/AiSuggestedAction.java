package com.aifleetops.ai.dto;

import com.aifleetops.ai.enums.SuggestionType;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 返回的单个操作建议，作为 {@link IncidentAnalysisResult#getSuggestedActions()} 的元素。
 * <p>
 * 相比 V0.2 的纯枚举，这里携带 type + payload + reason，
 * 让前端可以渲染成「可确认执行的预填充卡片」。
 * {@code suggestionId} 由后端落库后回填，供前端调用确认 / 拒绝接口；模型不会生成该字段。
 */
@Data
public class AiSuggestedAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 落库后回填的建议 ID，模型输出时为空 */
    private Long suggestionId;

    /** 动作类型 */
    private SuggestionType type;

    /** 预填充数据，具体结构见各类型 payload 规范 */
    private Map<String, Object> payload;

    /** AI 给出该建议的理由 */
    private String reason;
}
