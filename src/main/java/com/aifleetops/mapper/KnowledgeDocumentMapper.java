package com.aifleetops.mapper;

import com.aifleetops.entity.KnowledgeDocument;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    /**
     * 自然语言全文检索（ngram 解析器）。
     * 返回空列表时，调用方应走 LIKE / 全量兜底。
     */
    @Select("SELECT * FROM knowledge_documents " +
            "WHERE MATCH(title, content) AGAINST(#{query}) " +
            "ORDER BY id LIMIT #{limit}")
    List<KnowledgeDocument> searchByFullText(@Param("query") String query, @Param("limit") int limit);
}
