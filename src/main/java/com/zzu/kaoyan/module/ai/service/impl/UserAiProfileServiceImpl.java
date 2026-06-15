package com.zzu.kaoyan.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.ai.entity.UserAiProfile;
import com.zzu.kaoyan.module.ai.mapper.UserAiProfileMapper;
import com.zzu.kaoyan.module.ai.service.UserAiProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户 AI 档案服务实现。
 */
@Service
public class UserAiProfileServiceImpl implements UserAiProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserAiProfileServiceImpl.class);

    private final UserAiProfileMapper profileMapper;

    public UserAiProfileServiceImpl(UserAiProfileMapper profileMapper) {
        this.profileMapper = profileMapper;
    }

    @Override
    public boolean ensureProfile(Long userId) {
        UserAiProfile existing = profileMapper.selectOne(
                new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, userId));
        if (existing != null) {
            return true;
        }

        UserAiProfile profile = new UserAiProfile();
        profile.setUserId(userId);
        profile.setCognitiveProfile("{\"totalCheckDays\":0,\"continuousDays\":0,\"totalStudyHours\":0}");
        profile.setPsychologicalProfile("{\"recentEmotion\":\"未知\",\"history\":[]}");
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        profileMapper.insert(profile);

        log.info("UserAiProfile 已创建 — userId={}", userId);
        return false;
    }

    @Override
    public void updateCognitiveProfile(Long userId, int continuousDays, int totalCheckDays, int studyHours) {
        ensureProfile(userId);

        // 构建认知画像 JSON
        String cognitive = String.format(
                "{\"totalCheckDays\":%d,\"continuousDays\":%d,\"totalStudyHours\":%d,\"lastActive\":\"%s\"}",
                totalCheckDays, continuousDays, studyHours, LocalDateTime.now().toLocalDate());

        UserAiProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, userId));
        if (profile != null) {
            profile.setCognitiveProfile(cognitive);
            profile.setUpdatedAt(LocalDateTime.now());
            profileMapper.updateById(profile);
            log.info("UserAiProfile 认知画像已更新 — userId={}, cognitive={}", userId, cognitive);
        }
    }

    @Override
    public void updatePsychologicalProfile(Long userId, String emotionLabel, String summary) {
        ensureProfile(userId);

        // 构建心理画像 JSON
        String psychological = String.format(
                "{\"recentEmotion\":\"%s\",\"lastAnalysis\":\"%s\",\"updatedAt\":\"%s\"}",
                escapeJson(emotionLabel), escapeJson(summary), LocalDateTime.now());

        UserAiProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, userId));
        if (profile != null) {
            profile.setPsychologicalProfile(psychological);
            profile.setUpdatedAt(LocalDateTime.now());
            profileMapper.updateById(profile);
            log.info("UserAiProfile 心理画像已更新 — userId={}, emotion={}", userId, emotionLabel);
        }
    }

    /**
     * 简单的 JSON 字符串转义。
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
