package com.aifleetops.service;

import com.aifleetops.entity.KnowledgeDocument;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface KnowledgeDocumentService extends IService<KnowledgeDocument> {

    /**
     * 按问题检索知识库：先用全文检索，无结果时回退到返回全部（demo 文档少，由模型自行筛选）。
     */
    List<KnowledgeDocument> search(String question);
}
