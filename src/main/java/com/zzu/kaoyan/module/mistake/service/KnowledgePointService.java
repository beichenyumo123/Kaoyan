package com.zzu.kaoyan.module.mistake.service;

import com.zzu.kaoyan.module.mistake.entity.vo.KnowledgePointVO;

import java.util.List;

public interface KnowledgePointService {

    /**
     * 获取科目下的完整知识树
     * @param subject 科目，不传则返回全部
     */
    List<KnowledgePointVO> getTree(String subject);

    /**
     * 按关键词模糊搜索知识点
     */
    List<KnowledgePointVO> search(String keyword);

    /**
     * 从文本中匹配知识点（用于 OCR 增强）
     * @param text OCR 识别文本
     * @return 匹配到的知识点 ID 列表
     */
    List<Long> matchFromText(String text);
}
