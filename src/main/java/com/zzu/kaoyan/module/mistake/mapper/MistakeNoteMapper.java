package com.zzu.kaoyan.module.mistake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MistakeNoteMapper extends BaseMapper<MistakeNotePO> {

    /**
     * 查询用户最近的错题知识点摘要（用于 TutorAgent 薄弱点分析）。
     * 只查 knowledge_points 和 subject 字段，轻量查询。
     */
    @Select("SELECT knowledge_points, subject FROM mistake_note " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "AND knowledge_points IS NOT NULL AND knowledge_points != '' " +
            "ORDER BY created_at DESC LIMIT 30")
    List<Map<String, Object>> selectRecentKnowledgePoints(@Param("userId") Long userId);
}
