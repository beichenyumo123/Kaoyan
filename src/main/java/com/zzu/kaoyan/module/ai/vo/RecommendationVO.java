package com.zzu.kaoyan.module.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 智能推荐 VO
 */
@Data
public class RecommendationVO {

    /** 推荐的知识点 */
    private List<KnowledgeRecommendation> knowledgePoints;

    @Data
    public static class KnowledgeRecommendation {
        private Long id;
        private String title;
        private String subject;
        private String reason;
    }
}
