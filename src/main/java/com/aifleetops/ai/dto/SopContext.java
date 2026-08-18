package com.aifleetops.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * SOP 知识检索上下文。
 * <p>
 * 在 AI 数据流中，表示从知识库（RAG）中检索到的、与当前异常相关的标准作业流程片段。
 * 当前 V0.2.1 不会真正执行 RAG，该结构仅作为后续知识检索结果的数据契约，
 * 供 {@code IncidentAnalysisRequest} 携带给大模型作为参考依据。
 */
@Data
public class SopContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SOP 条目唯一标识 */
    private String sopId;

    /** SOP 标题 */
    private String sopTitle;

    /** 命中的 SOP 章节内容片段 */
    private List<String> matchedSections;

    /** 检索相关性得分 */
    private Double retrievalScore;
}
