package com.aifleetops.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 确认执行 AI 建议的请求体。
 */
@Data
public class AiSuggestionConfirmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作人，自由文本，可空 */
    private String confirmedBy;

    /** 工单描述覆盖，仅 CREATE_WORK_ORDER 生效，可空 */
    private String description;
}
