package com.zzu.kaoyan.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.ai.entity.AiMemoryEmbedding;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 语义记忆向量 Mapper。
 */
@Mapper
public interface AiMemoryEmbeddingMapper extends BaseMapper<AiMemoryEmbedding> {
}
