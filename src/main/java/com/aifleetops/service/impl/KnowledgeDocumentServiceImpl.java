package com.aifleetops.service.impl;

import com.aifleetops.entity.KnowledgeDocument;
import com.aifleetops.mapper.KnowledgeDocumentMapper;
import com.aifleetops.service.KnowledgeDocumentService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeDocumentServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeDocumentService {

    private static final int MAX_RESULTS = 5;

    @Override
    public List<KnowledgeDocument> search(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        List<KnowledgeDocument> hits = baseMapper.searchByFullText(question.trim(), MAX_RESULTS);
        if (!hits.isEmpty()) {
            return hits;
        }
        // 全文检索无结果（中文短句/停用词常见）：回退返回全部，模型自行筛选
        return list(Wrappers.<KnowledgeDocument>lambdaQuery()
                .orderByAsc(KnowledgeDocument::getId)
                .last("LIMIT " + MAX_RESULTS));
    }
}
