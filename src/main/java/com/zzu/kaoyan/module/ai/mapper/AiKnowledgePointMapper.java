package com.zzu.kaoyan.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.ai.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考研知识点 Mapper — 支持关键词检索（RAG 检索层）。
 */
@Mapper
public interface AiKnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    /**
     * 按关键词模糊检索知识点（RAG 核心方法）。
     * 匹配规则：标题、关键词、内容中包含任意一个搜索词。
     *
     * @param keywords 搜索关键词列表
     * @param subject  限定学科（可为 null，null 则全学科检索）
     * @param limit    返回条数上限
     * @return 匹配的知识点列表（按匹配度排序）
     */
    List<KnowledgePoint> searchByKeywords(@Param("keywords") List<String> keywords,
                                          @Param("subject") String subject,
                                          @Param("limit") int limit);
}
