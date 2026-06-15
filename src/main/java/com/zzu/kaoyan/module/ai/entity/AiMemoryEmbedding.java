package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 语义记忆向量 — 存储对话/错题的 embedding，用于跨会话语义检索。
 */
@Data
@TableName("ai_memory_embedding")
public class AiMemoryEmbedding {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 原文（问题 + 回答摘要），检索时用于展示 */
    private String content;

    /** 学科标签（MATH/408/ENGLISH/POLITICS），用于学科过滤 */
    private String subject;

    /** float[] 向量 JSON，维度取决于 Embedding 模型（text-embedding-v2: 1536） */
    private String embedding;

    /** CHAT_QA / MISTAKE_NOTE / KNOWLEDGE */
    private String sourceType;

    /** 关联的原数据 ID */
    private Long sourceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
