package com.zzu.kaoyan.module.ai.service;

/**
 * 用户 AI 档案服务 — 管理用户认知画像和心理画像的创建与更新。
 */
public interface UserAiProfileService {

    /**
     * 确保用户档案存在，不存在则创建。
     *
     * @param userId 用户ID
     * @return 档案是否存在（true=已存在，false=新建）
     */
    boolean ensureProfile(Long userId);

    /**
     * 更新用户认知画像（基于打卡数据）。
     *
     * @param userId         用户ID
     * @param continuousDays 连续打卡天数
     * @param totalCheckDays 总打卡天数
     * @param studyHours     今日学习时长
     */
    void updateCognitiveProfile(Long userId, int continuousDays, int totalCheckDays, int studyHours);

    /**
     * 更新用户心理画像（基于情绪分析结果）。
     *
     * @param userId       用户ID
     * @param emotionLabel 情绪标签（如"焦虑"、"积极"、"疲惫"）
     * @param summary      情绪分析摘要
     */
    void updatePsychologicalProfile(Long userId, String emotionLabel, String summary);
}
