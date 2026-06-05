package com.zzu.kaoyan.module.mistake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.mistake.entity.po.KnowledgePointPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePointPO> {

    @Select("SELECT * FROM knowledge_point WHERE is_deleted = 0 AND subject = #{subject} ORDER BY level, sort_order")
    List<KnowledgePointPO> selectBySubject(@Param("subject") String subject);

    @Select("SELECT * FROM knowledge_point WHERE is_deleted = 0 ORDER BY subject, level, sort_order")
    List<KnowledgePointPO> selectAll();

    @Select("SELECT * FROM knowledge_point WHERE is_deleted = 0 AND name LIKE CONCAT('%', #{keyword}, '%') ORDER BY level, sort_order LIMIT 50")
    List<KnowledgePointPO> searchByKeyword(@Param("keyword") String keyword);

    @Select("SELECT * FROM knowledge_point WHERE is_deleted = 0 AND parent_id = #{parentId} ORDER BY sort_order")
    List<KnowledgePointPO> selectByParentId(@Param("parentId") Long parentId);
}
