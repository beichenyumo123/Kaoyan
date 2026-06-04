package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考研知识点实体 — 答疑 Agent 的 RAG 知识源。
 */
@Data
@TableName("ai_knowledge_point")
public class KnowledgePoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学科 */
    private String subject;

    /** 章节 */
    private String chapter;

    /** 知识点标题 */
    private String title;

    /** 知识点详细内容 */
    private String content;

    /** 关键词（逗号分隔，用于检索） */
    private String keywords;

    /** 重要程度 HIGH/MEDIUM/LOW */
    private String importance;

    private LocalDateTime createdAt;
}
